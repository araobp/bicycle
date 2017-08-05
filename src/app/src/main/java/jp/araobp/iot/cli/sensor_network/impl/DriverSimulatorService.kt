package jp.araobp.iot.cli.sensor_network.impl

import android.os.Handler
import android.os.Message
import android.util.Log
import jp.araobp.iot.cli.sensor_network.SensorNetworkService

import jp.araobp.iot.messaging.MessageListenerActivity
import jp.araobp.iot.cli.sensor_network.Util
import jp.araobp.iot.cli.protocol.SensorNetworkProtocol

class DriverSimulatorService : SensorNetworkService() {
    private var mMessageListenerActivity: MessageListenerActivity? = null
    private var mHandler: Handler? = null

    private val mUtil = Util()

    private var mStarted = false
    private var mOpened = false
    private var mValue = 1000
    private var mSleep = TIMER * mValue


    override fun onCreate() {
        super.onCreate()

        mSleep = TIMER * mValue
        try {
            Thread(Runnable {
                while (true) {
                    if (mHandler != null && mOpened && mStarted) {
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

    override fun setMessageHandler(messageListenerActivity: MessageListenerActivity) {
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
        mOpened = true
        return true
    }

    override fun send(message: String) {
        if (mOpened) {
            mUtil.returnResponse("#" + message)
            val cmd = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (cmd[0]) {
                SensorNetworkProtocol.WHO -> mUtil.returnResponse("$:WHO:$DEVICE_NAME")
                SensorNetworkProtocol.STA -> mStarted = true
                SensorNetworkProtocol.STP -> mStarted = false
                SensorNetworkProtocol.GET -> mUtil.returnResponse("$:GET:" + mValue.toString())
                SensorNetworkProtocol.SET -> try {
                    mValue = Integer.parseInt(cmd[1])
                    mSleep = TIMER * mValue
                    Log.e(TAG, "mValue: " + mValue.toString())
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }

                SensorNetworkProtocol.MAP -> mUtil.returnResponse("$:MAP:$sDevices")
                SensorNetworkProtocol.RSC -> mUtil.returnResponse("$:RSC:$sSchedule")
            }
        }
    }

    override fun stop() {
        mStarted = false
    }

    override fun close() {
        mOpened = false
    }

    companion object {

        private val TAG = "Simulator"
        private val sDevices = "16,18,19"
        private val sSchedule = "0,0,0,0|19,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|18,0,0,0|0,0,0,0"

        val DEVICE_NAME = "SENSOR_SIMULATOR"

        val TIMER = 8  // 8msec
    }

}
