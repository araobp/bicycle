package jp.araobp.iot.sensor_network.service

import android.util.Log
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import jp.araobp.iot.sensor_network.SensorNetworkService
import kotlin.concurrent.thread

class SensorNetworkSimulator : SensorNetworkService() {

    private var mValue = 300
    private var mSleep = TIMER * mValue

    override fun onCreate() {
        super.onCreate()

        mSleep = TIMER * mValue
        try {
            thread(start = true) {
                var next = 0
                while (true) {
                    if (driverStatus.opened && driverStatus.started) {
                        try {
                            Thread.sleep(mSleep.toLong())
                        } catch (e: InterruptedException) {
                            Log.e(TAG, e.toString())
                        }
                        when(next) {
                            0 -> rx("%17:UINT8_T:0")
                            1 -> rx("%18:INT8_T:28,56")
                            2 -> rx("%19:FLOAT:-0.01,0.03,-0.01")
                        }
                        next = if (next >= 2) 0 else next + 1
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun open(baudrate: Int): Boolean {
        driverStatus.opened = true
        return true
    }

    override fun tx(message: String) {
        if (driverStatus.opened) {
            rx("#" + message)
            val cmd = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (cmd[0]) {
                SensorNetworkProtocol.WHO -> rx("$:WHO:${DEVICE_NAME}")
                SensorNetworkProtocol.STA -> driverStatus.started = true
                SensorNetworkProtocol.STP -> driverStatus.started = false
                SensorNetworkProtocol.GET -> rx("$:GET:" + mValue.toString())
                SensorNetworkProtocol.SET -> try {
                    mValue = Integer.parseInt(cmd[1])
                    mSleep = TIMER * mValue
                    Log.e(TAG, "mValue: " + mValue.toString())
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }

                SensorNetworkProtocol.MAP -> rx("$:MAP:${sDevices}")
                SensorNetworkProtocol.RSC -> rx("$:RSC:${sSchedule}")
            }
        }
    }

    override fun close() {
        driverStatus.opened = false
    }

    companion object {

        private val TAG = "Simulator"
        private val sDevices = "16,18,19"
        private val sSchedule = "0,0,0,0|19,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|18,0,0,0|0,0,0,0"

        val DEVICE_NAME = "SENSOR_SIMULATOR"

        val TIMER = 8  // 8msec
    }

}
