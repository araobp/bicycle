package jp.araobp.iot.edge_computing

import android.util.Log
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread


/**
 */
abstract class EdgeComputing {

    private val mWorkQueue = LinkedBlockingDeque<SensorNetworkEvent.SensorData>()
    protected val eventBus = EventBus.getDefault()

    private val TAG = "EdgeComputing"

    init {
        thread(start=true) {
            while (true) {
                var processedData: SensorNetworkEvent.ProcessedData? = process(mWorkQueue.take())
                if (processedData != null) {
                    Log.d(TAG, processedData.toString())
                    eventBus.post(processedData)
                }
            }
        }
    }

    fun onSensorData(sensorData: SensorNetworkEvent.SensorData) {
        mWorkQueue.add(sensorData)
    }

    protected abstract fun process(sensorData: SensorNetworkEvent.SensorData): SensorNetworkEvent.ProcessedData?

}