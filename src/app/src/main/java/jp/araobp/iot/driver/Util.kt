package jp.araobp.iot.driver

import android.os.Handler
import android.os.Message

/*
* Utilities
* */
class Util {

    private var mHandler: Handler? = null

    fun setHandler(handler: Handler) {
        mHandler = handler
    }

    fun returnResponse(message: String) {
        if (mHandler != null) {
            val msg = Message.obtain()
            msg.obj = message
            mHandler!!.sendMessage(msg)
        }
    }
}
