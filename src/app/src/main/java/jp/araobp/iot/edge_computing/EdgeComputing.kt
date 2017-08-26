package jp.araobp.iot.edge_computing

import android.util.Log
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread


/**
 * Edge computing abstract class
 */
abstract class EdgeComputing {

    private val mReceiveQueue = LinkedBlockingDeque<SensorNetworkEvent.SensorData>()

    private val mPeriodicReset = mutableMapOf<Int, MutableList<Long>>()

    companion object {
        const val RESET_CHECK_PERIOD: Long = 1_000
    }

    init {
        // FIFO queue of sensor data
        thread(start=true) {
            while (true) {
                process(mReceiveQueue.take())
            }
        }

        // sends reset signal to edge computing functions after expiration of reset check timer
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


    /**
     * Receives sensor data and puts it into receive queue
     */
    fun onSensorData(sensorData: SensorNetworkEvent.SensorData) {
        if (sensorData.deviceId in mPeriodicReset) {
            mPeriodicReset[sensorData.deviceId]!![1] = 0
        }
        mReceiveQueue.add(sensorData)
    }

    /**
     * Processes sensor data
     */
    protected abstract fun process(sensorData: SensorNetworkEvent.SensorData)

    /**
     * Registers periodic reset check to specific device ID
     */
    fun registerPeriodicReset(deviceId: Int, timer: Long) {
        mPeriodicReset[deviceId] = mutableListOf(timer, 0)
    }
}