package jp.araobp.iot.sensor_network

import android.os.Handler
import android.os.Message
import android.util.Log

class DriverSimulatorServiceImpl : SensorNetworkService() {
    private var mMessageListenerActivity: MessageListenerActivity? = null
    private var mHandler: Handler? = null

    private val mUtil = Util()

    private var mValue = 1000
    private var mSleep = TIMER * mValue

    private var mDriverStatus = DriverStatus(opened = false, started = false)

    override fun onCreate() {
        super.onCreate()

        mSleep = TIMER * mValue
        try {
            Thread(Runnable {
                while (true) {
                    if (mHandler != null && mDriverStatus.opened && mDriverStatus.started) {
                        try {
                            Thread.sleep(mSleep.toLong())
                        } catch (e: InterruptedException) {
                            Log.e(TAG, e.toString())
                        }

                        val msg = Message.obtain()
                        msg.obj = "%19:FLOAT:-0.01,0.03,-0.01"
                        mHandler!!.sendMessage(msg)
                    }
                }
            }).start()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun setMessageListenerActivity(messageListenerActivity: MessageListenerActivity) {
        this.mMessageListenerActivity = messageListenerActivity
        try {
            mHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    mMessageListenerActivity!!.onMessage(msg.obj as String)
                }
            }
            mUtil.setHandler(mHandler!!)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

    }

    override fun open(baudrate: Int): Boolean {
        mDriverStatus.opened = true
        return true
    }

    override fun send(message: String) {
        if (mDriverStatus.opened) {
            mUtil.returnResponse("#" + message)
            val cmd = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (cmd[0]) {
                SensorNetworkProtocol.WHO -> mUtil.returnResponse("$:WHO:${DEVICE_NAME}")
                SensorNetworkProtocol.STA -> mDriverStatus.started = true
                SensorNetworkProtocol.STP -> mDriverStatus.started = false
                SensorNetworkProtocol.GET -> mUtil.returnResponse("$:GET:" + mValue.toString())
                SensorNetworkProtocol.SET -> try {
                    mValue = Integer.parseInt(cmd[1])
                    mSleep = TIMER * mValue
                    Log.e(TAG, "mValue: " + mValue.toString())
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }

                SensorNetworkProtocol.MAP -> mUtil.returnResponse("$:MAP:${sDevices}")
                SensorNetworkProtocol.RSC -> mUtil.returnResponse("$:RSC:${sSchedule}")
            }
        }
    }

    override fun stop() {
        mDriverStatus.started = false
    }

    override fun close() {
        mDriverStatus.opened = false
    }

    override fun status(): DriverStatus {
        return mDriverStatus
    }

    companion object {

        private val TAG = "Simulator"
        private val sDevices = "16,18,19"
        private val sSchedule = "0,0,0,0|19,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|18,0,0,0|0,0,0,0"

        val DEVICE_NAME = "SENSOR_SIMULATOR"

        val TIMER = 8  // 8msec
    }

}
