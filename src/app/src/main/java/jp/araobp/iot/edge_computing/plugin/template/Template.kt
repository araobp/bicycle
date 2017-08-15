package jp.araobp.iot.edge_computing.plugin.template

import android.util.Log
import jp.araobp.iot.edge_computing.EdgeComputing
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import org.greenrobot.eventbus.EventBus

class Template: EdgeComputing() {

    companion object {
        private val TAG = javaClass.simpleName
    }

    /**
     * Data processed by edge computing
     *
     * @see jp.araobp.iot.edge_computing.EdgeComputing
     */
    data class ProcessedData(var timestamp: Long,
                             var deviceId: Int,
                             var data: List<Any>?)

    val mEventBus = EventBus.getDefault()

    override fun process(sensorData: SensorNetworkEvent.SensorData) {
        Log.d(TAG, sensorData.toString())
        var processedData: ProcessedData? = null
        var timestamp = System.currentTimeMillis()

        when (sensorData.deviceId) {
            // TODO: implementation
        }

        if (processedData != null) {
            Log.d(TAG, processedData.toString())
            mEventBus.post(processedData)
        }
    }
}