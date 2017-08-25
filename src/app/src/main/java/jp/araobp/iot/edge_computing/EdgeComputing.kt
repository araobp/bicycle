package jp.araobp.iot.edge_computing

import jp.araobp.iot.sensor_network.SensorNetworkEvent
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread


/**
 */
abstract class EdgeComputing {

    private val mWorkQueue = LinkedBlockingDeque<SensorNetworkEvent.SensorData>()

    private val mPeriodicReset = mutableMapOf<Int, MutableList<Long>>()

    companion object {
        const val RESET_CHECK_PERIOD: Long = 1_000
    }

    init {
        thread(start=true) {
            while (true) {
                process(mWorkQueue.take())
            }
        }

        thread(start = true) {
            while (true) {
                Thread.sleep(RESET_CHECK_PERIOD)
                mPeriodicReset.
                        filter{ ++it.value[1] >= it.value[0] }.
                        forEach {
                            it.value[1] = 0
                            val sensorData = SensorNetworkEvent.SensorData(
                                    deviceId = it.key,
                                    reset = true
                            )
                            onSensorData(sensorData)
                        }
            }
        }
    }


    fun onSensorData(sensorData: SensorNetworkEvent.SensorData) {
        mWorkQueue.add(sensorData)
    }

    protected abstract fun process(sensorData: SensorNetworkEvent.SensorData)

    fun registerPeriodicReset(deviceId: Int, timer: Long) {
        mPeriodicReset[deviceId] = mutableListOf(timer, 0)
    }
}