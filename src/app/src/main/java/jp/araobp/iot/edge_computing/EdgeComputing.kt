package jp.araobp.iot.edge_computing

import jp.araobp.iot.sensor_network.SensorNetworkEvent
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread


/**
 */
abstract class EdgeComputing {

    private val mWorkQueue = LinkedBlockingDeque<SensorNetworkEvent.SensorData>()

    init {
        thread(start=true) {
            while (true) {
                process(mWorkQueue.take())
            }
        }
    }

    fun onSensorData(sensorData: SensorNetworkEvent.SensorData) {
        mWorkQueue.add(sensorData)
    }

    protected abstract fun process(sensorData: SensorNetworkEvent.SensorData)

}