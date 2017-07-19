package jp.arao.bicycle.edge;

import android.os.Handler;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class FtdiDevice {

    private final String TAG = "Edge";

    private D2xxManager ftD2xx = null;
    private FT_Device ftDev = null;

    private boolean mThreadIsStopped = true;

    private final int READBUF_SIZE  = 256;
    private byte[] rbuf  = new byte[READBUF_SIZE];
    private char[] rchar = new char[READBUF_SIZE];
    private int mReadSize=0;

    private Handler mHandler = new Handler();
    private ReadListener readListener = null;

    public FtdiDevice(ReadListener readListener) {
        this.readListener = readListener;
        try {
            ftD2xx = D2xxManager.getInstance(readListener);
        } catch (D2xxManager.D2xxException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private void setConfig(int baudrate, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (!ftDev.isOpen()) {
            Log.e(TAG, "setConfig: device not open");
            return;
        }

        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baudrate);

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

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

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

        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
    }

    public boolean openDevice(int baudrate) {
        boolean update = false;

        // check if ftdi device has already benn instantiated
        if(ftDev != null) {
            if(ftDev.isOpen()) {
                if(mThreadIsStopped) {
                    update = true;
                    setConfig(baudrate, (byte)8, (byte)1, (byte)0, (byte)0);
                    ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                    ftDev.restartInTask();
                    new Thread(mLoop).start();
                }
                return update;
            }
        }

        int devCount = 0;
        devCount = ftD2xx.createDeviceInfoList(readListener);

        Log.d(TAG, "Device number : "+ Integer.toString(devCount));

        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        ftD2xx.getDeviceInfoList(devCount, deviceList);

        if(devCount <= 0) {
            return update;
        }

        if(ftDev == null) {
            ftDev = ftD2xx.openByIndex(readListener, 0);
        } else {
            synchronized (ftDev) {
                ftDev = ftD2xx.openByIndex(readListener, 0);
            }
        }

        if(ftDev.isOpen()) {
            if (mThreadIsStopped) {
                update = true;
                setConfig(baudrate, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDev.restartInTask();
                new Thread(mLoop).start();
            }
        }

        return update;
    }

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int i;
            int readSize;
            mThreadIsStopped = false;
            while(true) {
                if(mThreadIsStopped) {
                    break;
                }

                synchronized (ftDev) {
                    readSize = ftDev.getQueueStatus();
                    if(readSize>0) {
                        mReadSize = readSize;
                        if(mReadSize > READBUF_SIZE) {
                            mReadSize = READBUF_SIZE;
                        }
                        ftDev.read(rbuf,mReadSize);

                        for(i=0; i<mReadSize; i++) {
                            rchar[i] = (char)rbuf[i];
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                readListener.onRead(String.copyValueOf(rchar,0,mReadSize));
                            }
                        });

                    }
                }
            }
        }
    };

    public void write(String message) {
        if(ftDev == null) {
            return;
        }

        synchronized (ftDev) {
            if(ftDev.isOpen() == false) {
                Log.e(TAG, "onClickWrite : Device is not open");
                return;
            }

            ftDev.setLatencyTimer((byte)16);

            byte[] writeByte = message.getBytes();
            ftDev.write(writeByte, message.length());
        }
    }

    public void onDestroy() {
        mThreadIsStopped = true;
    }
    public void closeDevice() {
        mThreadIsStopped = true;
        if(ftDev != null) {
            ftDev.close();
        }
    }
}
