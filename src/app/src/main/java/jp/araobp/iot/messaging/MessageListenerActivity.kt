package jp.araobp.iot.messaging

import android.app.Activity

/*
* Sends data from sensor network to Activity.
* */
abstract class MessageListenerActivity : Activity() {

    abstract fun onMessage(message: String)

}
