package jp.araobp.iot.edge_computing.plugin.cycling

import android.util.Log
import jp.araobp.iot.edge_computing.EdgeComputing
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.thread

class Cycling: EdgeComputing() {

    companion object {
        private val TAG = "Cycling"
        const val DIAMETER = 20 * 2.5  // 20 inch : 50 cm
        const val SPEED_RESET_TIMER: Long = 1500 // 1.5sec
    }

    /**
     * Data processed by edge computing
     *
     * @see jp.araobp.iot.edge_computing.EdgeComputing
     */
    data class ProcessedData(var timestamp: Long,
                             var deviceId: Int,
                             var data: List<Any?>?)

    private val mEventBus = EventBus.getDefault()

    private var mLastTime = System.currentTimeMillis()

    private fun decimalFormat(value: Double, n: Int): Double {
        val precision = 10 * n
        return Math.round(value * precision).toDouble() / precision
    }

    override fun process(sensorData: SensorNetworkEvent.SensorData) {
        var processedData: ProcessedData? = null
        var timestamp = sensorData.timestamp

        when (sensorData.deviceId) {
            SensorNetworkProtocol.KXR94_2050,
            SensorNetworkProtocol.HDC1000,
            SensorNetworkProtocol.AMBIENT_TEMPERATURE,
            SensorNetworkProtocol.RELATIVE_HUMIDITY,
            SensorNetworkProtocol.ACCELEROMETER,
            SensorNetworkProtocol.FUSED_LOCATION -> {
                processedData = ProcessedData(
                        timestamp = sensorData.timestamp,
                        deviceId = sensorData.deviceId!!,
                        data = sensorData.data)
            }
            SensorNetworkProtocol.A1324LUA_T -> {
                var data = sensorData.data
                if (data!![0] as Int > 0) {
                    val elapsedTime = timestamp - mLastTime
                    mLastTime = timestamp
                    Log.d(TAG, "$timestamp:$data[0]")
                    var rpm = 60.0 * 1000.0 / elapsedTime
                    var speed = 60.0 * rpm * Math.PI * DIAMETER / 100.0 / 1000.0  // km/h
                    rpm = decimalFormat(rpm, 1)
                    speed = decimalFormat(speed, 1)
                    processedData = ProcessedData(
                            timestamp = timestamp,
                            deviceId = sensorData.deviceId!!,
                            data = listOf(rpm, speed))
                    val line1 = "%-16s".format("SPEED: $speed km/h")
                    val line2 = "%-16s".format("RPM: $rpm")
                    val displayMessage = SensorNetworkEvent.DisplayMessage(
                            deviceId = SensorNetworkProtocol.AQM1602XA_RN_GBW,
                            lines = listOf(line1, line2)
                    )
                    mEventBus.post(displayMessage)
                }
            }
        }
        Log.d(TAG, processedData.toString())
        if (processedData != null) {
            mEventBus.post(processedData)
        }
    }
}