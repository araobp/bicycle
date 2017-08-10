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

    val TAG = "SensorNetworkService"

    private var mRxHandler: Handler? = null
    protected var mRxHandlerActivity: RxHandlerActivity? = null

    private val mEdgeComputing: EdgeComputing = Cycling()

    data class SensorData(var timestamp: Long,
                          var rawData: String,
                          var deviceId: Int? = null,
                          var type: String? = null,
                          var data: List<String>? = null,
                          var schedulerInfo: SchedulerInfo? = null)

    data class SchedulerInfo(var infoType: InfoType? = null,
                             var timerScaler: Int? = 0,
                             var deviceMap: List<Int>? = null,
                             var schedule: List<List<Int>>? = null)

    enum class InfoType {
        TIMER_SCALER, DEVICE_MAP, SCHEDULE, STARTED, STOPPED
    }

    var driverStatus = DriverStatus(opened = false, started = false)

    private var mLoggingEnabled = false

    inner class ServiceBinder : Binder() {
        fun getService(): SensorNetworkService {
            return this@SensorNetworkService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SensorNetworkService started")
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
    * sends a received message to the handler
    * */
    protected fun rx(message: String) {
        var timestamp = System.currentTimeMillis()
        var sensorData = SensorData(timestamp = timestamp, rawData = message)
        val response = message.split(":".toRegex()).toList()

        fun sendMessage(msg: SensorData) {
            val msg = Message.obtain()
            msg.obj = sensorData
            mRxHandler?.sendMessage(msg)
        }

        when (message.substring(startIndex = 0, endIndex = 1)) {
            "%" -> {
                sensorData.deviceId = response[0].substring(1).toInt()
                sensorData.type = response[1]
                sensorData.data = response[2].split(",".toRegex()).toList()
                mEdgeComputing.onRx(sensorData)
                if (mLoggingEnabled) {
                    sendMessage(sensorData)
                }
            }
            "#" -> {
                when (message.substring(startIndex = 1, endIndex = 3)) {
                    SensorNetworkProtocol.STA -> {
                        sensorData.schedulerInfo = SchedulerInfo(infoType = InfoType.STARTED)
                        driverStatus.started = true
                    }
                    SensorNetworkProtocol.STP -> {
                        sensorData.schedulerInfo = SchedulerInfo(infoType = InfoType.STOPPED)
                        driverStatus.started = false
                    }
                }
                sendMessage(sensorData)
            }
            "$" -> {
                when (response[1]) {
                    SensorNetworkProtocol.GET -> sensorData.schedulerInfo = SchedulerInfo(
                            infoType = InfoType.TIMER_SCALER,
                            timerScaler = response[2].toInt()
                    )
                    SensorNetworkProtocol.MAP -> sensorData.schedulerInfo = SchedulerInfo(
                            infoType = InfoType.DEVICE_MAP,
                            deviceMap = response[2].split(",".toRegex()).toList().
                                    map { it.toInt() }.toList()
                    )
                    SensorNetworkProtocol.RSC -> sensorData.schedulerInfo = SchedulerInfo(
                            infoType = InfoType.SCHEDULE,
                            schedule = response[2].
                                    split("\\|".toRegex()).toList().
                                    map {
                                        it.split(",".toRegex()).map { it.toInt() }.toList()
                                    }.toList()
                    )
                }
                sendMessage(sensorData)
            }
        }
    }

    /*
    * opens the device driver
    * */
    abstract fun _open(baudrate: Int): Boolean
    fun open(baudrate: Int) {
        var opened = _open(baudrate)
        driverStatus.opened = opened
    }

    /*
    * writes a message to the device driver
    * */
    abstract fun _tx(message: String)
    fun tx(message: String) {
        _tx(message)
        when (message.substring(startIndex = 0, endIndex = 2)) {
            SensorNetworkProtocol.STA -> driverStatus.started = true
            SensorNetworkProtocol.STP -> driverStatus.started = false
        }
    }

    /*
    * closes the device driver
    * */
    abstract fun _close()
    fun close() {
        _close()
        driverStatus.opened = false
    }

    fun schedulerInfo() {
        try {
            Thread.sleep(CMD_SEND_INTERVAL)
            tx(SensorNetworkProtocol.GET)
            Thread.sleep(CMD_SEND_INTERVAL)
            tx(SensorNetworkProtocol.SCN)
            tx(SensorNetworkProtocol.MAP)
            Thread.sleep(CMD_SEND_INTERVAL)
            tx(SensorNetworkProtocol.RSC)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun startScheduler() {
        tx(SensorNetworkProtocol.STA)
        driverStatus.started = true
    }

    fun stopScheduler() {
        tx(SensorNetworkProtocol.STP)
        driverStatus.started = false
    }

    fun enableLogging(enabled: Boolean) {
        mLoggingEnabled = enabled
    }

    companion object {
        const val CMD_SEND_INTERVAL = 250L  // 250msec
    }
}