package jp.arao.iot.driver.imp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import jp.arao.iot.driver.ISensorNetworkDriver;
import jp.arao.iot.driver.ReadListener;
import jp.arao.iot.driver.Util;
import jp.arao.iot.protocol.Protocol;

public class SensorNetworkSimulator implements ISensorNetworkDriver{

    private static final String TAG = "Simulator";
    private ReadListener mReadListener = null;
    private Handler mHandler = null;

    private Util mUtil = null;

    private boolean mStarted = false;
    private boolean mOpened = false;
    private String mValue = "1";
    private static final String sDevices = "16,18,19";
    private static final String sSchedule = "0,0,0,0|19,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|18,0,0,0|0,0,0,0";

    public SensorNetworkSimulator() {
        mUtil = new Util();
        mUtil.returnResponse("Sensor network simulator started");
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (mHandler != null && mStarted) {
                            try {
                                Thread.sleep(1000);
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

    private void log(String message) {
        Message msg = Message.obtain();
        msg.obj = message;
        mHandler.sendMessage(msg);
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
            switch (message) {
                case Protocol.STA:
                    mStarted = true;
                    break;
                case Protocol.STP:
                    mStarted = false;
                    break;
                case Protocol.GET:
                    mUtil.returnResponse("$:GET:" + mValue);
                    break;
                case Protocol.SET:
                    try {
                        mValue = message.split(":")[1];
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
        mStarted = false;
        mOpened = false;
    }

}
