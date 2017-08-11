package jp.araobp.iot.sensor_network

import android.app.Activity

/**
* Sends rawData from sensor network to Activity.
*/
abstract class SensorDataHandlerActivity : Activity() {

    abstract fun onSensorData(sensorData: SensorNetworkService.SensorData)

}
