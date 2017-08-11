package jp.araobp.iot.edge_computing

import android.util.Log
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import jp.araobp.iot.sensor_network.SensorNetworkService
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread

class Cycling: EdgeComputing() {
    private val TAG = "Cycling"

    override fun process(sensorData: SensorNetworkService.SensorData) {
        Log.d(TAG, sensorData.toString())
        when(sensorData.deviceId) {
            SensorNetworkProtocol.KXR94_2050 -> {
                var threeAxisData = sensorData.data?.map{ it.toFloat() }?.toList()

            }
        }

    }
}