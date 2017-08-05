package jp.araobp.iot.cli.sensor_network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import jp.araobp.iot.messaging.MessageListenerActivity

abstract class SensorNetworkService: Service() {

    val mBinder: IBinder = ServiceBinder()

    val TAG_Parent = "SensorNetworkService"

    inner class ServiceBinder : Binder() {
        fun getService(): SensorNetworkService {
            return this@SensorNetworkService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG_Parent, "SensorNetworkService started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    /*
    * sets callback method that receives messages one by one from Handler/Looper
    * */
    abstract fun setMessageHandler(messageListenerActivity: MessageListenerActivity)

    /*
    * opens the device driver
    * */
    abstract fun open(baudrate: Int): Boolean

    /*
    * writes a message to the device driver
    * */
    abstract fun send(message: String)

    /*
    * stops running the device driver
    * */
    abstract fun stop()

    /*
    * closes the device driver
    * */
    abstract fun close()

}