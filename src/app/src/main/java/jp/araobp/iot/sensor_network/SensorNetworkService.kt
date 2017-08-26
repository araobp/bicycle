package jp.araobp.iot.sensor_network

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*
import jp.araobp.iot.cli.CliActivity
import jp.araobp.iot.edge_computing.EdgeComputing
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.reflect.full.primaryConstructor

/**
 * Sensor network service
 */
abstract class SensorNetworkService: Service(), SensorEventListener {

    companion object {
        private val TAG = "SensorNetworkService"
        const val CMD_SEND_INTERVAL = 250L  // 250msec
        const val G_UNIT = 9.90665  // 9.80665 m/s^2
        const val BUILTIN_SENSOR_TEMPERATURE_INTERVAL = 5000_000  // 5sec
        const val BUILTIN_SENSOR_HUMIDITY_INTERVAL = 5000_000  // 5sec
        const val BUILTIN_SENSOR_ACCELEROMETER_INTERVAL = 500_000  // 500msec
    }

    data class DriverStatus(var opened: Boolean = false, var started: Boolean = false, var currentDeviceId: Int = 0)

    var driverStatus = DriverStatus(opened = false, started = false, currentDeviceId = 0)

    private val mBinder: IBinder = ServiceBinder()
    private var mEdgeComputing: EdgeComputing? = null
    private var mLoggingEnabled = false
    private val mEventBus = EventBus.getDefault()

    private var mFusedLocationClient: FusedLocationProviderClient? = null

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
        Log.d(TAG, "onBind called")
        val EDGE_COMPUTING_CLASS = intent.extras["edge_computing_class"] as String
        val sEdgeComputingClass = Class.forName(EDGE_COMPUTING_CLASS).kotlin
        mEdgeComputing = sEdgeComputingClass.primaryConstructor!!.call() as EdgeComputing

        val mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (intent.extras[CliActivity.AMBIENT_TEMPERATURE] == true) {
            val temperature: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            temperature?.let {
                mSensorManager.registerListener(this, temperature,
                        BUILTIN_SENSOR_TEMPERATURE_INTERVAL, BUILTIN_SENSOR_TEMPERATURE_INTERVAL)
            }
        }
        if (intent.extras[CliActivity.RELATIVE_HUMIDITY] == true) {
            val humidity: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
            humidity?.let {
                mSensorManager.registerListener(this, humidity,
                        BUILTIN_SENSOR_HUMIDITY_INTERVAL, BUILTIN_SENSOR_HUMIDITY_INTERVAL)
            }
        }
        if (intent.extras[CliActivity.ACCELEROMETER] == true) {
            val accelerometer: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                mSensorManager.registerListener(this, accelerometer,
                        BUILTIN_SENSOR_ACCELEROMETER_INTERVAL, BUILTIN_SENSOR_ACCELEROMETER_INTERVAL)
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Call back func to receives location data and transfers it to edge computing functions
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                var sensorData = SensorNetworkEvent.SensorData(rawData = locationResult.toString())
                sensorData.deviceId = SensorNetworkProtocol.FUSED_LOCATION
                sensorData.data = listOf(locationResult?.lastLocation?.latitude, locationResult?.lastLocation?.longitude)
                sensorData.type = SensorNetworkProtocol.DOUBLE
                Log.d("onLocationResult", locationResult.toString())
                mEdgeComputing?.onSensorData(sensorData)
            }
        }

        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, locationCallback, null)

        EventBus.getDefault().register(this)

        return mBinder
    }

    val mLocationRequest: LocationRequest = LocationRequest().
            setFastestInterval(5000).
            setInterval(5000).
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    /**
     * Receives data from the sensor network and parses it
     */
    protected fun rx(message: String) {
        var sensorData = SensorNetworkEvent.SensorData(rawData = message)
        val response = message.split(":".toRegex()).toList()

        when (message.substring(startIndex = 0, endIndex = 1)) {
            "%" -> {
                Log.d(TAG, response[0])
                sensorData.deviceId = response[0].substring(1).toInt()
                sensorData.type = response[1]
                val dataStringList: List<String> = response[2].split(",".toRegex()).toList()
                when (sensorData.type) {
                    SensorNetworkProtocol.FLOAT -> sensorData.data = dataStringList.map { it.toFloat() }.toList()
                    SensorNetworkProtocol.INT8_T, SensorNetworkProtocol.UINT8_T,
                    SensorNetworkProtocol.INT16_T, SensorNetworkProtocol.UINT16_T
                    -> sensorData.data = dataStringList.map { it.toInt() }.toList()
                }
                mEdgeComputing?.onSensorData(sensorData)
                if (mLoggingEnabled) {
                    mEventBus.post(sensorData)
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
                mEventBus.post(sensorData)
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
                mEventBus.post(sensorData)
            }
        }
    }

    /**
     * Receives onAccuracyChanged events from Android's sensor manager
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "sensor accuracy changed to " + accuracy.toString())
    }

    /**
     * Receives onSensorChanged events from Android's sensor manager
     */
    override fun onSensorChanged(event: SensorEvent) {
        //event.values.forEach { Log.d(TAG, it.toString()) }
        if (driverStatus.started) {
            var sensorData = SensorNetworkEvent.SensorData(timestamp = event.timestamp, rawData = event.values[0].toString())
            when (event.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    sensorData.deviceId = SensorNetworkProtocol.AMBIENT_TEMPERATURE
                    sensorData.data = event.values.map { it.toInt() }.toList()
                    sensorData.type = SensorNetworkProtocol.INT8_T
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    sensorData.deviceId = SensorNetworkProtocol.RELATIVE_HUMIDITY
                    sensorData.data = event.values.map { it.toInt() }.toList()
                    sensorData.type = SensorNetworkProtocol.INT8_T
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    sensorData.deviceId = SensorNetworkProtocol.ACCELEROMETER
                    sensorData.data = event.values.map { (it / G_UNIT * 100).toInt().toFloat() / 100.0 }.toList()
                    sensorData.type = SensorNetworkProtocol.FLOAT
                }
            }
            Log.d(TAG, sensorData.toString())
            mEdgeComputing?.onSensorData(sensorData)
        }
    }

    // *** Commands to scheduler ******************************************************************
    // Bound activities call these APIs to send commands to scheduler

    /**
     * Opens the device driver
     */
    protected abstract fun open(baudrate: Int): Boolean

    fun openDevice(baudrate: Int) {
        var opened = open(baudrate)
        driverStatus.opened = opened
    }

    /**
     * Transmits data to the sensor network
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
     * Closes the device driver
     */
    protected abstract fun close()

    fun closeDevice() {
        close()
        driverStatus.opened = false
    }

    /**
     * Sends sensor data to SensorDataHandlerActivity
     */
    fun enableLogging(enabled: Boolean) {
        mLoggingEnabled = enabled
    }

    /**
     * Fetches scheduler-related info from the sensor network
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
     * Starts running the sensor network
     */
    fun startScheduler() {
        transmit(SensorNetworkProtocol.STA)
        driverStatus.started = true
    }

    /**
     * Stops running the sensor network
     */
    fun stopScheduler() {
        transmit(SensorNetworkProtocol.STP)
        driverStatus.started = false
    }

    // *** Commands to actuators ******************************************************************
    // Edge computing classes call these APIs to send commands to actuators

    /**
     * Displays message to LCD such as AQM1602XA_RN_GBW
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDisplayMessage(displayMessage: SensorNetworkEvent.DisplayMessage) {
        if (driverStatus.currentDeviceId != displayMessage.deviceId) {
            transmit("${SensorNetworkProtocol.I2C}:${displayMessage.deviceId}")
            driverStatus.currentDeviceId = displayMessage.deviceId
            Thread.sleep(CMD_SEND_INTERVAL)
        }
        when(displayMessage.deviceId) {
            SensorNetworkProtocol.AQM1602XA_RN_GBW -> {
                if (displayMessage.lines.size > 2 || displayMessage.lines.isEmpty()) {
                    Log.e(TAG, "Illegal number of lines")
                } else {
                    val line1 = displayMessage.lines[0]
                    val line2 = if (displayMessage.lines.size == 2) displayMessage.lines[1] else ""
                    val cmd = "${SensorNetworkProtocol.DSP}:%-16s%-16s".format(line1, line2)
                    transmit(cmd)
                    Log.d(TAG, cmd)
                }
            }
        }
    }
}