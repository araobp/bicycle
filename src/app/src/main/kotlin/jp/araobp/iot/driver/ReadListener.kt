package jp.araobp.iot.driver

import android.app.Activity

/*
* Sends data from sensor network to Activity.
* */
abstract class ReadListener : Activity() {

    abstract fun onRead(output: String)

}
