package jp.araobp.iot.sensor_network

/**
 * Events on EventBus
 */
class SensorNetworkEvent {

    /**
     * Sensor data from sensor network
     */
    data class SensorData(var timestamp: Long,
                          var rawData: String,
                          var deviceId: Int? = null,
                          var type: String? = null,
                          var data: List<String>? = null,
                          var schedulerInfo: SchedulerInfo? = null)

    /**
     * Scheduler info
     */
    data class SchedulerInfo(var schedulerInfoType: SchedulerInfoType? = null,
                             var timerScaler: Int? = 0,
                             var deviceMap: List<Int>? = null,
                             var schedule: List<List<Int>>? = null)

    /**
     * Scheduler info type
     */
    enum class SchedulerInfoType {
        TIMER_SCALER, DEVICE_MAP, SCHEDULE, STARTED, STOPPED
    }

    /**
     * Data processed by edge computing
     *
     * @see jp.araobp.iot.edge_computing.EdgeComputing
     */
    data class ProcessedData(var timestamp: Long,
                             var deviceId: Int,
                             var data: List<Any>?)
}