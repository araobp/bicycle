package jp.araobp.iot.sensor_network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import jp.araobp.iot.edge_computing.logic.Cycling
import jp.araobp.iot.edge_computing.EdgeComputing
import org.greenrobot.eventbus.EventBus

/**
 * Sensor network service
 */
abstract class SensorNetworkService: Service() {

    val TAG = javaClass.simpleName

    data class DriverStatus(var opened: Boolean = false, var started: Boolean = false)
    var driverStatus = DriverStatus(opened = false, started = false)

    private val mBinder: IBinder = ServiceBinder()
    private val mEdgeComputing: EdgeComputing = Cycling()
    private var mLoggingEnabled = false
    private val eventBus = EventBus.getDefault()

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

    /**
     * receives data from the sensor network and parses it
     */
    protected fun rx(message: String) {
        var timestamp = System.currentTimeMillis()
        var sensorData = SensorNetworkEvent.SensorData(timestamp = timestamp, rawData = message)
        val response = message.split(":".toRegex()).toList()

        when (message.substring(startIndex = 0, endIndex = 1)) {
            "%" -> {
                sensorData.deviceId = response[0].substring(1).toInt()
                sensorData.type = response[1]
                sensorData.data = response[2].split(",".toRegex()).toList()
                mEdgeComputing.onSensorData(sensorData)
                if (mLoggingEnabled) {
                    eventBus.post(sensorData)
                }
            }
            "#" -> {
                when (message.substring(startIndex = 1, endIndex = 3)) {
                    SensorNetworkProtocol.STA -> {
                        sensorData.schedulerInfo = SensorNetworkEvent.SchedulerInfo(schedulerInfoType = SensorNetworkEvent.SchedulerInfoType.STARTED)
                        driverStatus.started = true
                    }
                    SensorNetworkProtocol.STP -> {
                        sensorData.schedulerInfo = SensorNetworkEvent.SchedulerInfo(schedulerInfoType = SensorNetworkEvent.SchedulerInfoType.STOPPED)
                        driverStatus.started = false
                    }
                }
                eventBus.post(sensorData)
            }
            "$" -> {
                when (response[1]) {
                    SensorNetworkProtocol.GET -> sensorData.schedulerInfo = SensorNetworkEvent.SchedulerInfo(
                            schedulerInfoType = SensorNetworkEvent.SchedulerInfoType.TIMER_SCALER,
                            timerScaler = response[2].toInt()
                    )
                    SensorNetworkProtocol.MAP -> sensorData.schedulerInfo = SensorNetworkEvent.SchedulerInfo(
                            schedulerInfoType = SensorNetworkEvent.SchedulerInfoType.DEVICE_MAP,
                            deviceMap = response[2].split(",".toRegex()).toList().
                                    map { it.toInt() }.toList()
                    )
                    SensorNetworkProtocol.RSC -> sensorData.schedulerInfo = SensorNetworkEvent.SchedulerInfo(
                            schedulerInfoType = SensorNetworkEvent.SchedulerInfoType.SCHEDULE,
                            schedule = response[2].
                                    split("\\|".toRegex()).toList().
                                    map {
                                        it.split(",".toRegex()).map { it.toInt() }.toList()
                                    }.toList()
                    )
                }
                eventBus.post(sensorData)
            }
        }
    }

    /**
     * opens the device driver
     */
    protected abstract fun open(baudrate: Int): Boolean
    fun openDevice(baudrate: Int) {
        var opened = open(baudrate)
        driverStatus.opened = opened
    }

    /**
     * transmits data to the sensor network
     */
    protected abstract fun tx(message: String)
    fun transmit(message: String) {
        tx(message)
        when (message.substring(startIndex = 0, endIndex = 2)) {
            SensorNetworkProtocol.STA -> driverStatus.started = true
            SensorNetworkProtocol.STP -> driverStatus.started = false
        }
    }

    /**
     * closes the device driver
     */
    protected abstract fun close()
    fun closeDevice() {
        close()
        driverStatus.opened = false
    }

    /**
     * fetches scheduler-related info from the sensor network
     *
     * @see SensorData
     * @see rx
     */
    fun fetchSchedulerInfo() {
        try {
            Thread.sleep(CMD_SEND_INTERVAL)
            transmit(SensorNetworkProtocol.GET)
            Thread.sleep(CMD_SEND_INTERVAL)
            transmit(SensorNetworkProtocol.SCN)
            transmit(SensorNetworkProtocol.MAP)
            Thread.sleep(CMD_SEND_INTERVAL)
            transmit(SensorNetworkProtocol.RSC)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * starts running the sensor network
     */
    fun startScheduler() {
        transmit(SensorNetworkProtocol.STA)
        driverStatus.started = true
    }

    /**
     * stops running the sensor network
     */
    fun stopScheduler() {
        transmit(SensorNetworkProtocol.STP)
        driverStatus.started = false
    }

    /**
     * sends sensor data to SensorDataHandlerActivity
     */
    fun enableLogging(enabled: Boolean) {
        mLoggingEnabled = enabled
    }

    private companion object {
        const val CMD_SEND_INTERVAL = 250L  // 250msec
    }
}