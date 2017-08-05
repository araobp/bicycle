package jp.araobp.iot.cli.driver

import jp.araobp.iot.messaging.MessageListenerActivity

/*
* Sensor network driver interface
* */
interface ISensorNetworkDriver {

    /*
    * sets callback method that receives messages one by one from Handler/Looper
    * */
    fun setReadListener(messageListenerActivity: MessageListenerActivity)

    /*
    * opens the device driver
    * */
    fun open(baudrate: Int): Boolean

    /*
    * writes a message to the device driver
    * */
    fun write(message: String)

    /*
    * stops running the device driver
    * */
    fun stop()

    /*
    * closes the device driver
    * */
    fun close()

}
