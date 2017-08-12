package jp.araobp.iot.sensor_network

class SensorNetworkEvent {
    data class SensorData(var timestamp: Long,
                          var rawData: String,
                          var deviceId: Int? = null,
                          var type: String? = null,
                          var data: List<String>? = null,
                          var schedulerInfo: SchedulerInfo? = null)

    data class SchedulerInfo(var infoType: InfoType? = null,
                             var timerScaler: Int? = 0,
                             var deviceMap: List<Int>? = null,
                             var schedule: List<List<Int>>? = null)

    enum class InfoType {
        TIMER_SCALER, DEVICE_MAP, SCHEDULE, STARTED, STOPPED
    }

    data class ProcessedData(var timestamp: Long,
                             var deviceId: Int,
                             var data: List<Any>?)
}