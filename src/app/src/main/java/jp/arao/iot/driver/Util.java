package jp.arao.iot.driver;

import android.os.Handler;
import android.os.Message;

public class Util {

    private Handler mHandler = null;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void returnResponse(String message) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.obj = message;
            mHandler.sendMessage(msg);
        }
    }
}
