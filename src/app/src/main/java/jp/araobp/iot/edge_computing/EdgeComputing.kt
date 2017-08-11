package jp.araobp.iot.edge_computing

import jp.araobp.iot.sensor_network.SensorNetworkService

interface EdgeComputing {

    fun onSensorData(sensorData: SensorNetworkService.SensorData)

}