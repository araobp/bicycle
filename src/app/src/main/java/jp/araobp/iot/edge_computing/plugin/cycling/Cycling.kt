package jp.araobp.iot.edge_computing.plugin.cycling

import android.util.Log
import jp.araobp.iot.edge_computing.EdgeComputing
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.timer

class Cycling: EdgeComputing() {

    companion object {
        private val TAG = "Cycling"
        val DIAMETER = 20 * 2.5  // 20 inch : 50 cm
    }

    /**
     * Data processed by edge computing
     *
     * @see jp.araobp.iot.edge_computing.EdgeComputing
     */
    data class ProcessedData(var timestamp: Long,
                             var deviceId: Int,
                             var data: List<Any?>?)

    val mEventBus = EventBus.getDefault()

    var lasttime = System.currentTimeMillis()

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
                    val elapsedTime = timestamp - lasttime
                    lasttime = timestamp
                    Log.d(TAG, "$timestamp:$data[0]")
                    var rpm = 60.0 * 1000.0 / elapsedTime
                    var speed = 60.0 * rpm * Math.PI * DIAMETER / 100.0 / 1000.0  // km/h
                    rpm = ((rpm * 10).toInt()/10).toDouble()
                    speed = ((speed * 10).toInt()/10).toDouble()
                    processedData = ProcessedData(
                            timestamp = timestamp,
                            deviceId = sensorData.deviceId!!,
                            data = listOf(rpm, speed))
                }
            }
        }
        Log.d(TAG, processedData.toString())
        if (processedData != null) {
            mEventBus.post(processedData)
        }
    }
}