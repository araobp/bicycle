package jp.araobp.iot.driver.imp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import jp.araobp.iot.driver.ISensorNetworkDriver;
import jp.araobp.iot.driver.ReadListener;
import jp.araobp.iot.driver.Util;
import jp.araobp.iot.protocol.Protocol;

public class SensorNetworkSimulator implements ISensorNetworkDriver{

    private static final String TAG = "Simulator";
    private ReadListener mReadListener = null;
    private Handler mHandler = null;

    private Util mUtil = null;

    private boolean mStarted = false;
    private boolean mOpened = false;
    private int mValue = 1000;
    private static final String sDevices = "16,18,19";
    private static final String sSchedule = "0,0,0,0|19,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|18,0,0,0|0,0,0,0";

    public static final String DEVICE_NAME = "SENSOR_SIMULATOR";

    public static final int TIMER = 8;  // 8msec
    private int mSleep = TIMER * mValue;

    public SensorNetworkSimulator() {
        mUtil = new Util();
        mSleep = TIMER * mValue;
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (mHandler != null && mOpened && mStarted) {
                            try {
                                Thread.sleep(mSleep);
                            } catch (InterruptedException e) {
                                Log.e(TAG, e.toString());
                            }
                            Message msg = Message.obtain();
                            msg.obj = "%19:FLOAT:-0.01,0.03,-0.01";
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void setReadListener(ReadListener readListener) {
        this.mReadListener = readListener;
        try {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    mReadListener.onRead((String)msg.obj);
                }
            };
            mUtil.setHandler(mHandler);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean open(int baudrate) {
        mOpened = true;
        return true;
    }

    public void write(String message) {
        if (mOpened) {
            mUtil.returnResponse("#" + message);
            String cmd[] = message.split(":");
            switch (cmd[0]) {
                case Protocol.WHO:
                    mUtil.returnResponse("$:WHO:" + DEVICE_NAME);
                    break;
                case Protocol.STA:
                    mStarted = true;
                    break;
                case Protocol.STP:
                    mStarted = false;
                    break;
                case Protocol.GET:
                    mUtil.returnResponse("$:GET:" + String.valueOf(mValue));
                    break;
                case Protocol.SET:
                    try {
                        mValue = Integer.parseInt(cmd[1]);
                        mSleep = TIMER * mValue;
                        Log.e(TAG, "mValue: " + String.valueOf(mValue));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
                case Protocol.MAP:
                    mUtil.returnResponse("$:MAP:" + sDevices);
                    break;
                case Protocol.RSC:
                    mUtil.returnResponse("$:RSC:" + sSchedule);
                    break;
            }
        }
    }

    public void stop() {
        mStarted = false;
    }

    public void close() {
        mOpened = false;
    }

}
