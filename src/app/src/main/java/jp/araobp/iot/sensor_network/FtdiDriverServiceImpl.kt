package jp.araobp.iot.sensor_network

import android.os.Handler
import android.os.Message
import android.util.Log

import com.ftdi.j2xx.D2xxManager
import com.ftdi.j2xx.FT_Device

import kotlin.experimental.or



/*
* FTDI device driver
*
* */
class FtdiDriverServiceImpl : SensorNetworkService() {

    private val TAG = "CLI"

    private var mD2xxManager: D2xxManager? = null
    private var mFtdiDevice: FT_Device? = null

    private var mReaderIsRunning = false

    private val mReadBuf = ByteArray(READBUF_SIZE)
    private val mCharBuf = CharArray(READBUF_SIZE)
    private var mReadLen = 0

    private var mHandler: Handler? = null
    private var mMessageListenerActivity: MessageListenerActivity? = null

    private var mUtil: Util? = null
    private var mDriverStatus = DriverStatus(opened = false, started = false)

    override fun setMessageListenerActivity(messageListenerActivity: MessageListenerActivity) {
        mMessageListenerActivity = messageListenerActivity
        mUtil = Util()
        try {
            mD2xxManager = D2xxManager.getInstance(messageListenerActivity.applicationContext)
            mHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    if (mMessageListenerActivity != null) {
                        mMessageListenerActivity!!.onMessage(msg.obj as String)
                    }
                }
            }
            mUtil!!.setHandler(mHandler!!)
        } catch (e: D2xxManager.D2xxException) {
            Log.e(TAG, e.toString())
        }

    }

    /*
    * set FTDI device config
    * */
    private fun setConfig(baudrate: Int, dataBits: Byte, stopBits: Byte, parity: Byte, flowControl: Byte) {
        val dataBitsByte: Byte
        val stopBitsByte: Byte
        val parityByte: Byte
        if (!mFtdiDevice!!.isOpen) {
            Log.e(TAG, "setConfig: device not open")
            return
        }

        mFtdiDevice!!.setBitMode(0.toByte(), D2xxManager.FT_BITMODE_RESET)

        mFtdiDevice!!.setBaudRate(baudrate)

        when (dataBits) {
            7.toByte() -> dataBitsByte = D2xxManager.FT_DATA_BITS_7
            8.toByte() -> dataBitsByte = D2xxManager.FT_DATA_BITS_8
            else -> dataBitsByte = D2xxManager.FT_DATA_BITS_8
        }

        when (stopBits) {
            1.toByte() -> stopBitsByte = D2xxManager.FT_STOP_BITS_1
            2.toByte() -> stopBitsByte = D2xxManager.FT_STOP_BITS_2
            else -> stopBitsByte = D2xxManager.FT_STOP_BITS_1
        }

        when (parity) {
            0.toByte() -> parityByte = D2xxManager.FT_PARITY_NONE
            1.toByte() -> parityByte = D2xxManager.FT_PARITY_ODD
            2.toByte() -> parityByte = D2xxManager.FT_PARITY_EVEN
            3.toByte() -> parityByte = D2xxManager.FT_PARITY_MARK
            4.toByte() -> parityByte = D2xxManager.FT_PARITY_SPACE
            else -> parityByte = D2xxManager.FT_PARITY_NONE
        }

        mFtdiDevice!!.setDataCharacteristics(dataBitsByte, stopBitsByte, parityByte)

        val flowCtrlSetting: Short
        when (flowControl) {
            0.toByte() -> flowCtrlSetting = D2xxManager.FT_FLOW_NONE
            1.toByte() -> flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS
            2.toByte() -> flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR
            3.toByte() -> flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF
            else -> flowCtrlSetting = D2xxManager.FT_FLOW_NONE
        }

        mFtdiDevice!!.setFlowControl(flowCtrlSetting, 0x0b.toByte(), 0x0d.toByte())
    }

    // reader thread
    private val mReader = Runnable {
        var i: Int
        var len: Int
        var offset = 0
        var c: Char
        mReaderIsRunning = true
        while (true) {
            if (!mReaderIsRunning) {
                break
            }

            synchronized(mFtdiDevice as FT_Device) {
                len = mFtdiDevice!!.queueStatus
                if (len > 0) {
                    mReadLen = len
                    if (mReadLen > READBUF_SIZE) {
                        mReadLen = READBUF_SIZE
                    }
                    mFtdiDevice!!.read(mReadBuf, mReadLen)

                    i = 0
                    while (i < mReadLen) {
                        c = mReadBuf[i].toChar()
                        mCharBuf[offset++] = c
                        if (c == sDelimiter) {
                            if (offset >= 3) {
                                val msg = Message.obtain()
                                msg.obj = String(mCharBuf, 0, offset - 1)
                                mHandler!!.sendMessage(msg)
                            }
                            offset = 0
                        }
                        i++
                    }
                }
            }
        }
    }

    /*
    * Opens FTDI device and start reader thread
    *
    * @parameter baudrate baud rate
    * @return true if reader thread's state is changed
    * */
    override fun open(baudrate: Int): Boolean {
        var stateChanged = false

        if (mFtdiDevice != null) {
            if (mFtdiDevice!!.isOpen) {
                if (!mReaderIsRunning) {
                    stateChanged = true
                    setConfig(baudrate, 8.toByte(), 1.toByte(), 0.toByte(), 0.toByte())
                    mFtdiDevice!!.purge((D2xxManager.FT_PURGE_TX or D2xxManager.FT_PURGE_RX).toByte())
                    mFtdiDevice!!.restartInTask()
                    Thread(mReader).start()
                }
                return stateChanged
            }
        }

        var devCount = mD2xxManager!!.createDeviceInfoList(mMessageListenerActivity)

        Log.d(TAG, "Device number : " + Integer.toString(devCount))

        val deviceList = arrayOfNulls<D2xxManager.FtDeviceInfoListNode>(devCount)
        mD2xxManager!!.getDeviceInfoList(devCount, deviceList)

        if (devCount <= 0) {
            return stateChanged
        }

        if (mFtdiDevice == null) {
            mFtdiDevice = mD2xxManager!!.openByIndex(mMessageListenerActivity, 0)
        } else {
            synchronized(mFtdiDevice as FT_Device) {
                mFtdiDevice = mD2xxManager!!.openByIndex(mMessageListenerActivity, 0)
            }
        }

        if (mFtdiDevice!!.isOpen) {
            mDriverStatus.opened = true
            if (!mReaderIsRunning) {
                stateChanged = true
                setConfig(baudrate, 8.toByte(), 1.toByte(), 0.toByte(), 0.toByte())
                mFtdiDevice!!.purge((D2xxManager.FT_PURGE_TX or D2xxManager.FT_PURGE_RX).toByte())
                mFtdiDevice!!.restartInTask()
                Thread(mReader).start()
            }
        }

        return stateChanged
    }

    /*
    * Sends message to FTDI device
    * */
    override fun send(message: String) {
        val data = message + DELIMITER
        if (mFtdiDevice == null) {
            return
        }

        synchronized(mFtdiDevice as FT_Device) {
            if (mFtdiDevice!!.isOpen == false) {
                Log.e(TAG, "onClickWrite : device is not open")
                return
            }

            mFtdiDevice!!.latencyTimer = 16.toByte()

            val writeByte = data.toByteArray()
            mFtdiDevice!!.write(writeByte, data.length)

            val cmd = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (cmd[0]) {
                SensorNetworkProtocol.STA -> mDriverStatus.started = true
                SensorNetworkProtocol.STP -> mDriverStatus.started = false
            }
        }
    }

    /*
    * Stops reader thread
    * */
    override fun stop() {
        mReaderIsRunning = false
        mDriverStatus.started = false
    }

    /*
    * Stops reader thread and closes FTDI device
    * */
    override fun close() {
        mReaderIsRunning = false
        if (mFtdiDevice != null) {
            mFtdiDevice!!.close()
            mDriverStatus.opened = false
        }
    }

    override fun status(): DriverStatus {
        return mDriverStatus
    }

    companion object {

        // Size of read buffer (the number of characters)
        val READBUF_SIZE = 1024

        val DELIMITER = "\n"
        private val sDelimiter = '\n'
    }
}
