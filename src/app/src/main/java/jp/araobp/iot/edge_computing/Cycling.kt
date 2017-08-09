package jp.araobp.iot.edge_computing

import android.util.Log
import jp.araobp.iot.sensor_network.SensorNetworkService
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread

class Cycling: EdgeComputing {

    val TAG = "Cycling"
    val fifoQueue = LinkedBlockingDeque<SensorNetworkService.SensorData>()

    init {
        thread(start = true) {
            while (true) {
                var sensorData = fifoQueue.take()
                Log.d(TAG, sensorData.toString())
            }
        }
    }

    override fun onRx(sensorData: SensorNetworkService.SensorData) {
        fifoQueue.add(sensorData)
    }

}