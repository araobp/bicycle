package jp.araobp.iot.driver.impl

import android.os.Handler
import android.os.Message
import android.util.Log

import jp.araobp.iot.driver.ISensorNetworkDriver
import jp.araobp.iot.driver.ReadListener
import jp.araobp.iot.driver.Util
import jp.araobp.iot.protocol.Protocol

class SensorNetworkSimulator : ISensorNetworkDriver {
    private var mReadListener: ReadListener? = null
    private var mHandler: Handler? = null

    private val mUtil = Util()

    private var mStarted = false
    private var mOpened = false
    private var mValue = 1000
    private var mSleep = TIMER * mValue

    init {
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

    override fun setReadListener(readListener: ReadListener) {
        this.mReadListener = readListener
        try {
            mHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    mReadListener!!.onRead(msg.obj as String)
                }
            }
            mUtil!!.setHandler(mHandler!!)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

    }

    override fun open(baudrate: Int): Boolean {
        mOpened = true
        return true
    }

    override fun write(message: String) {
        if (mOpened) {
            mUtil!!.returnResponse("#" + message)
            val cmd = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (cmd[0]) {
                Protocol.WHO -> mUtil.returnResponse("$:WHO:$DEVICE_NAME")
                Protocol.STA -> mStarted = true
                Protocol.STP -> mStarted = false
                Protocol.GET -> mUtil.returnResponse("$:GET:" + mValue.toString())
                Protocol.SET -> try {
                    mValue = Integer.parseInt(cmd[1])
                    mSleep = TIMER * mValue
                    Log.e(TAG, "mValue: " + mValue.toString())
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }

                Protocol.MAP -> mUtil.returnResponse("$:MAP:$sDevices")
                Protocol.RSC -> mUtil.returnResponse("$:RSC:$sSchedule")
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
