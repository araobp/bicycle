package jp.araobp.iot.cli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton

import java.util.ArrayList

import jp.araobp.iot.driver.impl.SensorNetworkDriverImpl
import jp.araobp.iot.driver.ISensorNetworkDriver
import jp.araobp.iot.driver.ReadListener
import jp.araobp.iot.driver.impl.SensorNetworkSimulator
import jp.araobp.iot.protocol.Protocol

/*
* Sensor Network CLI
*
* @see <a href="https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md">https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md</a>
* */
class MainActivity : ReadListener() {

    private var mBaudrate = 0

    private var mDriver: ISensorNetworkDriver? = null

    private var mTextView: TextView? = null
    private var mEditText: EditText? = null
    private var mButtonOpen: Button? = null
    private var mButtonWrite: Button? = null
    private var mToggleButton: ToggleButton? = null
    private var mCheckBoxBaudrate9600: CheckBox? = null
    private var mCheckBoxSimualtor: CheckBox? = null
    private var mSwitch: Switch? = null
    private var mTextViewScaler: TextView? = null
    private var mTextViewDevices: TextView? = null
    private val mListSchedules = ArrayList<TextView>()

    private var mResponseLoggingEnabled = false

    private var mOpened = false
    private var mStarted = false

    internal var mTimerScaler = "unknown"

    private fun log(message: String) {
        mTextView!!.append(message + "\n")
    }

    private fun startCommunication(): Boolean {
        var update = false
        if (mCheckBoxSimualtor!!.isChecked) {
            log("Initializing sensor network simulator")
            if (mDriver == null || mDriver is SensorNetworkDriverImpl) {
                mDriver = SensorNetworkSimulator()
            }
        } else {
            log("Initializing sensor network driver")
            if (mDriver == null || mDriver is SensorNetworkSimulator) {
                mDriver = SensorNetworkDriverImpl()
            }
        }
        if (mDriver != null) {
            mDriver!!.setReadListener(this)
            update = mDriver!!.open(mBaudrate)
            log(if (update) "Sensor network connected" else "Unable to connect sensor network")
            try {
                Thread.sleep(CMD_SEND_INTERVAL.toLong())
                mDriver!!.write(Protocol.GET)
                Thread.sleep(CMD_SEND_INTERVAL.toLong())
                mDriver!!.write(Protocol.SCN)
                mDriver!!.write(Protocol.MAP)
                Thread.sleep(CMD_SEND_INTERVAL.toLong())
                mDriver!!.write(Protocol.RSC)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        }
        return update
    }

    private fun updateButtonText(on: Boolean) {
        if (on) {
            mButtonOpen!!.text = sButtonOpenClose
            mButtonWrite!!.isEnabled = true
        } else {
            mButtonOpen!!.text = sButtonOpenOpen
            mButtonWrite!!.isEnabled = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        updateButtonText(startCommunication())
    }

    internal var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                updateButtonText(startCommunication())
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                mDriver!!.close()
                mSwitch!!.isChecked = false
                updateButtonText(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextView = findViewById(R.id.textViewRead) as TextView
        mEditText = findViewById(R.id.editTextWrite) as EditText

        mButtonOpen = findViewById(R.id.buttonOpen) as Button
        mButtonWrite = findViewById(R.id.buttonWrite) as Button
        mToggleButton = findViewById(R.id.toggleButton) as ToggleButton

        mCheckBoxBaudrate9600 = findViewById(R.id.checkBoxBaudrate9600) as CheckBox
        mCheckBoxSimualtor = findViewById(R.id.checkBoxSimulator) as CheckBox

        mSwitch = findViewById(R.id.switchStart) as Switch
        mTextViewScaler = findViewById(R.id.textViewScaler) as TextView
        mTextViewScaler!!.text = mTimerScaler

        mTextViewDevices = findViewById(R.id.textViewDevices) as TextView

        mListSchedules.add(findViewById(R.id.textViewSchedule1) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule2) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule3) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule4) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule5) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule6) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule7) as TextView)

        updateButtonText(false)

        mButtonOpen!!.setOnClickListener {
            if (mButtonOpen!!.text == sButtonOpenOpen) {
                updateButtonText(startCommunication())
                mOpened = true
                if (mStarted) {
                    mSwitch!!.isChecked = true
                }
            } else {
                mDriver!!.close()
                mOpened = false
                updateButtonText(false)
                if (mStarted) {
                    mSwitch!!.isChecked = false
                }
            }
        }

        mButtonWrite!!.setOnClickListener {
            val writeString = mEditText!!.text.toString().toUpperCase()
            mDriver!!.write(writeString)
            mEditText!!.setText("")
        }

        mBaudrate = if (mCheckBoxBaudrate9600!!.isChecked) DEFAULT_BAUDRATE else SCHEDULER_BAUDRATE

        mCheckBoxBaudrate9600!!.setOnCheckedChangeListener { _, _ ->
            if (mCheckBoxBaudrate9600!!.isChecked) {
                mBaudrate = DEFAULT_BAUDRATE
            } else {
                mBaudrate = SCHEDULER_BAUDRATE
            }
            Log.d(TAG, Integer.toString(mBaudrate))
        }

        mSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (mDriver != null) {
                if (isChecked) {
                    log("Switch on")
                    mDriver!!.write(Protocol.STA)
                    mSwitch!!.isChecked = true
                    mStarted = true
                } else {
                    log("Switch off")
                    if (mOpened) {
                        mDriver!!.write(Protocol.STP)
                        mStarted = false
                    }
                    mSwitch!!.isChecked = false
                }
            }
        }

        mToggleButton!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {  // OFF
                log("Logging disabled")
                mResponseLoggingEnabled = false
            } else {  // ON
                log("Logging enabled")
                mTextView!!.text = ""
                mResponseLoggingEnabled = true
            }
        }

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(mUsbReceiver, filter)
    }

    override fun onRead(message: String) {
        if (mResponseLoggingEnabled) {
            log(message)
        }
        if (message.startsWith("$")) {
            val response = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (response[1]) {
                Protocol.STA -> {
                    mTimerScaler = response[2]
                    mTextViewScaler!!.text = mTimerScaler
                }
                Protocol.MAP -> {
                    mTextViewDevices!!.text = ""
                    mTextViewDevices!!.append(response[2])
                }
                Protocol.RSC -> {
                    val schs = response[2].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in schs.indices) {
                        mListSchedules[i].text = schs[i]
                    }
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        mDriver!!.stop()
        unregisterReceiver(mUsbReceiver)
    }

    companion object {

        val DEFAULT_BAUDRATE = 9600  // 9600kbps
        val SCHEDULER_BAUDRATE = 115200  // 115200kbps
        val CMD_SEND_INTERVAL = 250  // 250msec

        private val TAG = "CLI"

        private val sButtonOpenOpen = "Open"
        private val sButtonOpenClose = "Close"
    }
}
