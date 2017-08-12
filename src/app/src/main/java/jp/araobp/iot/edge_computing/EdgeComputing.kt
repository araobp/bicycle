package jp.araobp.iot.edge_computing

import jp.araobp.iot.sensor_network.Event
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread


/**
 */
abstract class EdgeComputing {

    private val mWorkQueue = LinkedBlockingDeque<Event.SensorData>()
    protected val eventBus = EventBus.getDefault()

    init {
        thread(start=true) {
            while (true) {
                var processedData: Event.ProcessedData? = process(mWorkQueue.take())
                eventBus.post(processedData)
            }
        }
    }

    fun onSensorData(sensorData: Event.SensorData) {
        mWorkQueue.add(sensorData)
    }

    protected abstract fun process(sensorData: Event.SensorData): Event.ProcessedData?

}