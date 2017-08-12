package jp.araobp.iot.edge_computing.logic

import android.util.Log
import jp.araobp.iot.edge_computing.EdgeComputing
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import jp.araobp.iot.sensor_network.SensorNetworkProtocol

class Cycling: EdgeComputing() {
    private val TAG = "Cycling"

    override fun process(sensorData: SensorNetworkEvent.SensorData): SensorNetworkEvent.ProcessedData? {
        Log.d(TAG, sensorData.toString())
        var processedData: SensorNetworkEvent.ProcessedData? = null
        var timestamp = System.currentTimeMillis()

        when (sensorData.deviceId) {
            SensorNetworkProtocol.KXR94_2050 -> {
                var threeAxisData = sensorData.data?.map { it.toFloat() }?.toList()
                processedData = SensorNetworkEvent.ProcessedData(
                        timestamp = timestamp,
                        deviceId = sensorData.deviceId!!,
                        data = threeAxisData)
                eventBus.post(processedData)
            }
            SensorNetworkProtocol.A1324LUA_T -> {

            }
            SensorNetworkProtocol.HDC1000 -> {

            }
        }
        return processedData
    }
}