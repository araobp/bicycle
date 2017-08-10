package jp.araobp.iot.sensor_network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import jp.araobp.iot.edge_computing.Cycling
import jp.araobp.iot.edge_computing.EdgeComputing

abstract class SensorNetworkService: Service() {

    data class DriverStatus(var opened: Boolean = false, var started: Boolean = false)

    private val mBinder: IBinder = ServiceBinder()

    val TAG_Parent = "SensorNetworkService"
    var enabled = false

    private var mRxHandler: Handler? = null
    protected var mRxHandlerActivity: RxHandlerActivity? = null

    private val mEdgeComputing: EdgeComputing = Cycling()

    data class SensorData(var timestamp: Long,
                          var rawData: String,
                          var deviceId: Int? = null,
                          var type: String? = null,
                          var data: List<String>? = null)

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
    fun setRxHandlerActivity(rxHandlerActivity: RxHandlerActivity) {
        mRxHandlerActivity = rxHandlerActivity
        mRxHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (mRxHandlerActivity != null) {
                    mRxHandlerActivity!!.onRx(msg.obj as SensorData)
                }
            }
        }
    }

    /*
    * sends a received message to the listener
    * */
    protected fun rx(message: String) {
        var timestamp = System.currentTimeMillis()
        var sensorData = SensorData(timestamp=timestamp, rawData =message)

        if (message.startsWith("%")) {
            val response = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            sensorData.deviceId = response[0].substring(1).toInt()
            sensorData.type = response[1]
            sensorData.data = response[2].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toList()
            mEdgeComputing.onRx(sensorData)
            if (!enabled) return
        }

        if (mRxHandler != null) {
            val msg = Message.obtain()
            msg.obj = sensorData
            mRxHandler!!.sendMessage(msg)
        }

    }

    /*
    * opens the device driver
    * */
    abstract fun open(baudrate: Int): Boolean

    /*
    * writes a message to the device driver
    * */
    abstract fun tx(message: String)

    /*
    * stops running the device driver
    * */
    abstract fun stop()

    /*
    * closes the device driver
    * */
    abstract fun close()

    /*
    * returns current driver status
    * */
    abstract fun status(): DriverStatus
}