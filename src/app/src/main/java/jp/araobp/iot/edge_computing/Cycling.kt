package jp.araobp.iot.edge_computing

import android.util.Log
import jp.araobp.iot.sensor_network.Event
import jp.araobp.iot.sensor_network.SensorNetworkProtocol

class Cycling: EdgeComputing() {
    private val TAG = "Cycling"

    override fun process(sensorData: Event.SensorData): Event.ProcessedData? {
        Log.d(TAG, sensorData.toString())
        var processedData: Event.ProcessedData? = null
        var timestamp = System.currentTimeMillis()

        when (sensorData.deviceId) {
            SensorNetworkProtocol.KXR94_2050 -> {
                var threeAxisData = sensorData.data?.map { it.toFloat() }?.toList()
                processedData = Event.ProcessedData(
                        timestamp = timestamp,
                        deviceId = sensorData.deviceId!!,
                        data = threeAxisData)
                eventBus.post(processedData)
            }
        }
        return processedData
    }
}