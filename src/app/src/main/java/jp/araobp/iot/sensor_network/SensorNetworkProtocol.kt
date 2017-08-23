package jp.araobp.iot.sensor_network

/**
* Plug&Play protocol
*
* @see <a href="https://github.com/araobp/sensor-network">https://github.com/araobp/sensor-network</a>
*/
object SensorNetworkProtocol {

    const val STA = "STA"
    const val STP = "STP"
    const val RSC = "RSC"
    const val GET = "GET"
    const val SET = "SET"
    const val I2C = "I2C"
    const val WHO = "WHO"
    const val MAP = "MAP"
    const val SCN = "SCN"

    const val FLOAT             = "FLOAT"
    const val INT8_T            = "INT8_T"
    const val UINT8_T           = "UINT8_T"
    const val INT16_T           = "INT16_T"
    const val UINT16_T          = "UINT16_T"
    const val DOUBLE            = "DOUBLE"  // for android-built-in sensors

    // Sensors on the sensor network
    const val AQM1602XA_RN_GBW  = 16
    const val A1324LUA_T        = 17
    const val HDC1000           = 18
    const val KXR94_2050        = 19
    const val SHT31_DIS         = 20

    // Android-built-in sensors
    const val AMBIENT_TEMPERATURE = 64
    const val RELATIVE_HUMIDITY = 65
    const val ACCELEROMETER = 66
    const val FUSED_LOCATION = 67

}
