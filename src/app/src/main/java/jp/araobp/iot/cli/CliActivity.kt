package jp.araobp.iot.cli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton

import java.util.ArrayList

import jp.araobp.iot.cli.driver.impl.SensorNetworkDriverImpl
import jp.araobp.iot.cli.driver.ISensorNetworkDriver
import jp.araobp.iot.messaging.MessageListenerActivity
import jp.araobp.iot.cli.driver.impl.SensorNetworkSimulator
import jp.araobp.iot.cli.protocol.SensorNetworkProtocol
import jp.araobp.iot.edge_computing.EdgeService
import android.content.ComponentName
import android.content.ServiceConnection

/*
* Sensor Network CLI
*
* @see <a href="https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md">https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md</a>
* */
class CliActivity : MessageListenerActivity() {

    private var mBaudrate = 0

    private var mDriver: ISensorNetworkDriver? = null

    private var mTextView: TextView? = null
    private var mEditText: EditText? = null
    private var mButtonOpen: Button? = null
    private var mButtonWrite: Button? = null
    private var mToggleButtonLog: ToggleButton? = null
    private var mToggleButtonEdge: ToggleButton? = null
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

    private var mEdgeService: EdgeService? = null
    private var mEdgeServiceBound = false

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
                mDriver!!.write(SensorNetworkProtocol.GET)
                Thread.sleep(CMD_SEND_INTERVAL.toLong())
                mDriver!!.write(SensorNetworkProtocol.SCN)
                mDriver!!.write(SensorNetworkProtocol.MAP)
                Thread.sleep(CMD_SEND_INTERVAL.toLong())
                mDriver!!.write(SensorNetworkProtocol.RSC)
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
        setContentView(R.layout.activity_cli)

        mTextView = findViewById(R.id.textViewRead) as TextView
        mEditText = findViewById(R.id.editTextWrite) as EditText

        mButtonOpen = findViewById(R.id.buttonOpen) as Button
        mButtonWrite = findViewById(R.id.buttonWrite) as Button
        mToggleButtonLog = findViewById(R.id.toggleButtonLog) as ToggleButton
        mToggleButtonEdge = findViewById(R.id.toggleButtonEdge) as ToggleButton

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
                    mDriver!!.write(SensorNetworkProtocol.STA)
                    mSwitch!!.isChecked = true
                    mStarted = true
                } else {
                    log("Switch off")
                    if (mOpened) {
                        mDriver!!.write(SensorNetworkProtocol.STP)
                        mStarted = false
                    }
                    mSwitch!!.isChecked = false
                }
            }
        }

        mToggleButtonEdge!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                log("Edge computing enabled")
                // Bind to LocalService
                val intent = Intent(this, EdgeService::class.java)
                bindService(intent, mEdgeServiceConnection, Context.BIND_AUTO_CREATE)
            } else {
                log("Edge computing disabled")
                if (mEdgeServiceBound) {
                    unbindService(mEdgeServiceConnection)
                    mEdgeServiceBound = false
                }
            }
        }

        mToggleButtonLog!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                log("Logging enabled")
                mResponseLoggingEnabled = true
            } else {
                log("Logging disabled")
                mResponseLoggingEnabled = false
            }
        }

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(mUsbReceiver, filter)
    }

    private val mEdgeServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as EdgeService.EdgeServiceBinder
            mEdgeService = binder.getService()
            if (mEdgeService != null) {
                log("Edge computing started")
                mEdgeService?.test("Hello")
            } else {
                log("Failed to start edge computing")
            }
            mEdgeServiceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mEdgeServiceBound = false
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onMessage(message: String) {
        if (mResponseLoggingEnabled) {
            log(message)
        }
        if (message.startsWith("$")) {
            val response = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (response[1]) {
                SensorNetworkProtocol.STA -> {
                    mTimerScaler = response[2]
                    mTextViewScaler!!.text = mTimerScaler
                }
                SensorNetworkProtocol.MAP -> {
                    mTextViewDevices!!.text = ""
                    mTextViewDevices!!.append(response[2])
                }
                SensorNetworkProtocol.RSC -> {
                    val schs = response[2].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in schs.indices) {
                        mListSchedules[i].text = schs[i]
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mEdgeServiceBound) {
            unbindService(mEdgeServiceConnection)
            mEdgeServiceBound = false
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        mDriver?.stop()
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
