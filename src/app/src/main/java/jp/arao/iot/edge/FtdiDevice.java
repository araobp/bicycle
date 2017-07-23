package jp.arao.iot.edge;

import android.os.Handler;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

/*
* FTDI device
* */
public class FtdiDevice {

    // Size of read buffer (the number of characters)
    public static final int READBUF_SIZE  = 256;

    private final String TAG = "Edge";

    private D2xxManager mD2xxManager = null;
    private FT_Device mFtDevice = null;

    private boolean mReaderIsRunning = false;

    private byte[] mReadBuf = new byte[READBUF_SIZE];
    private char[] mCharBuf = new char[READBUF_SIZE];
    private int mReadSize=0;

    private Handler mHandler = new Handler();
    private ReadListener mReadListener = null;

    public static final String DELIMITER = "\n";

    /*
    * constructor
    *
    * @parameter readListener instance of ReadListener
    * */
    public FtdiDevice(ReadListener readListener) {
        this.mReadListener = readListener;
        try {
            mD2xxManager = D2xxManager.getInstance(readListener);
        } catch (D2xxManager.D2xxException e) {
            Log.e(TAG, e.toString());
        }
    }

    /*
    * set FTDI device config
    * */
    private void setConfig(int baudrate, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (!mFtDevice.isOpen()) {
            Log.e(TAG, "setConfig: device not open");
            return;
        }

        mFtDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        mFtDevice.setBaudRate(baudrate);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        mFtDevice.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        mFtDevice.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
    }


    // reader thread
    private Runnable mReader = new Runnable() {
        @Override
        public void run() {
            int i;
            int readSize;
            mReaderIsRunning = true;
            while(true) {
                if(!mReaderIsRunning) {
                    break;
                }

                synchronized (mFtDevice) {
                    readSize = mFtDevice.getQueueStatus();
                    if(readSize>0) {
                        mReadSize = readSize;
                        if(mReadSize > READBUF_SIZE) {
                            mReadSize = READBUF_SIZE;
                        }
                        mFtDevice.read(mReadBuf,mReadSize);

                        for(i=0; i<mReadSize; i++) {
                            mCharBuf[i] = (char) mReadBuf[i];
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                mReadListener.onRead(String.copyValueOf(mCharBuf,0,mReadSize));
                            }
                        });

                    }
                }
            }
        }
    };

    /*
    * Opens FTDI device and start reader thread
    *
    * @parameter baudrate baud rate
    * @return true if reader thread's state is changed
    * */
    public boolean open(int baudrate) {
        boolean stateChanged = false;

        if(mFtDevice != null) {
            if(mFtDevice.isOpen()) {
                if(!mReaderIsRunning) {
                    stateChanged = true;
                    setConfig(baudrate, (byte)8, (byte)1, (byte)0, (byte)0);
                    mFtDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                    mFtDevice.restartInTask();
                    new Thread(mReader).start();
                }
                return stateChanged;
            }
        }

        int devCount = 0;
        devCount = mD2xxManager.createDeviceInfoList(mReadListener);

        Log.d(TAG, "Device number : "+ Integer.toString(devCount));

        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        mD2xxManager.getDeviceInfoList(devCount, deviceList);

        if(devCount <= 0) {
            return stateChanged;
        }

        if(mFtDevice == null) {
            mFtDevice = mD2xxManager.openByIndex(mReadListener, 0);
        } else {
            synchronized (mFtDevice) {
                mFtDevice = mD2xxManager.openByIndex(mReadListener, 0);
            }
        }

        if(mFtDevice.isOpen()) {
            if (!mReaderIsRunning) {
                stateChanged = true;
                setConfig(baudrate, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                mFtDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                mFtDevice.restartInTask();
                new Thread(mReader).start();
            }
        }

        return stateChanged;
    }

    /*
    * Sends message to FTDI device
    * */
    public void write(String message) {
        String data = message + DELIMITER;
        if(mFtDevice == null) {
            return;
        }

        synchronized (mFtDevice) {
            if(mFtDevice.isOpen() == false) {
                Log.e(TAG, "onClickWrite : device is not open");
                return;
            }

            mFtDevice.setLatencyTimer((byte)16);

            byte[] writeByte = data.getBytes();
            mFtDevice.write(writeByte, data.length());
        }
    }

    /*
    * Stops reader thread
    * */
    public void stop() {
        mReaderIsRunning = false;
    }

    /*
    * Stops reader thread and closes FTDI device
    * */
    public void close() {
        mReaderIsRunning = false;
        if(mFtDevice != null) {
            mFtDevice.close();
        }
    }
}
