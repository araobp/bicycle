package jp.araobp.iot.ui

import android.app.Activity
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

import jp.araobp.iot.sensor_network.service.FtdiDriver
import jp.araobp.iot.sensor_network.service.SensorNetworkSimulator
import android.content.ComponentName
import android.content.ServiceConnection
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import jp.araobp.iot.sensor_network.SensorNetworkService
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus



/**
* Sensor Network CLI
*
* @see <a href="https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md">https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md</a>
*/
class CliActivity : Activity() {

    private val TAG = javaClass.simpleName

    private var mTextView: TextView? = null
    private var mEditText: EditText? = null
    private var mButtonOpen: Button? = null
    private var mButtonWrite: Button? = null
    private var mToggleButtonLog: ToggleButton? = null
    private var mCheckBoxBaudrate9600: CheckBox? = null
    private var mCheckBoxSimualtor: CheckBox? = null
    private var mSwitch: Switch? = null
    private var mTextViewScaler: TextView? = null
    private var mTextViewDevices: TextView? = null
    private val mListSchedules = ArrayList<TextView>()
    private var mButtonVisualizerCycling: Button? = null

    private var mBaudrate = 0
    private var mTimerScaler = "unknown"

    private var mSensorNetworkService: SensorNetworkService? = null
    private var mSensorNetworkServiceBound = false

    private val sButtonOpenOpen = "Open"
    private val sButtonOpenClose = "Close"

    companion object {
        const val DEFAULT_BAUDRATE = 9600  // 9600kbps
        const val SCHEDULER_BAUDRATE = 115200  // 115200kbps
    }

    private fun log(message: String) {
        mTextView!!.append(message + "\n")
    }

    private fun startSensorNetworkService() {
        log("start communication")
        mSwitch!!.isChecked = false
        val intent: Intent?
        if (mCheckBoxSimualtor!!.isChecked) {
            intent = Intent(this, SensorNetworkSimulator::class.java)
        } else {
            intent = Intent(this, FtdiDriver::class.java)
        }
        bindService(intent, mSensorNetworkServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopSensorNetworkService() {
        if (mSensorNetworkService != null) {
            mSensorNetworkService!!.closeDevice()
            unbindService(mSensorNetworkServiceConnection)
            val intent = Intent(this, SensorNetworkService::class.java)
            stopService(intent)
        }
    }

    private fun toggleButtonText(opened: Boolean) {
        if (opened) {
            mButtonOpen!!.text = sButtonOpenClose
            mButtonWrite!!.isEnabled = true
        } else {
            mButtonOpen!!.text = sButtonOpenOpen
            mButtonWrite!!.isEnabled = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        //startSensorNetworkService()
    }

    internal var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                //startSensorNetworkService()
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                stopSensorNetworkService()
                mSwitch!!.isChecked = false
                toggleButtonText(false)
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

        mCheckBoxBaudrate9600 = findViewById(R.id.checkBoxBaudrate9600) as CheckBox
        mCheckBoxSimualtor = findViewById(R.id.checkBoxSimulator) as CheckBox

        mSwitch = findViewById(R.id.switchStart) as Switch
        mTextViewScaler = findViewById(R.id.textViewScaler) as TextView
        mTextViewScaler!!.text = mTimerScaler

        mTextViewDevices = findViewById(R.id.textViewDevices) as TextView

        mButtonVisualizerCycling = findViewById(R.id.buttonVisualizerCycling) as Button

        mListSchedules.add(findViewById(R.id.textViewSchedule1) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule2) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule3) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule4) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule5) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule6) as TextView)
        mListSchedules.add(findViewById(R.id.textViewSchedule7) as TextView)

        toggleButtonText(false)

        mButtonOpen!!.setOnClickListener {
            if (mButtonOpen!!.text == sButtonOpenOpen) {
                startSensorNetworkService()
            } else {
                toggleButtonText(false)
                mSwitch!!.isChecked = !mSensorNetworkService!!.driverStatus.started
                stopSensorNetworkService()
            }
        }

        mButtonWrite!!.setOnClickListener {
            val writeString = mEditText!!.text.toString().toUpperCase()
            mSensorNetworkService?.transmit(writeString)
            mEditText!!.setText("")
        }

        mBaudrate = if (mCheckBoxBaudrate9600!!.isChecked) DEFAULT_BAUDRATE else SCHEDULER_BAUDRATE

        mCheckBoxBaudrate9600!!.setOnCheckedChangeListener { _, isChecked ->
            mBaudrate = if (isChecked) DEFAULT_BAUDRATE else SCHEDULER_BAUDRATE
            Log.d(TAG, Integer.toString(mBaudrate))
        }

        mSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (mSensorNetworkService != null) {
                if (isChecked) {
                    log("Switch on")
                    mSensorNetworkService!!.startScheduler()
                    mSwitch!!.isChecked = true
                } else {
                    log("Switch off")
                    if (mSensorNetworkService!!.driverStatus.opened) {
                        mSensorNetworkService!!.stopScheduler()
                    }
                    mSwitch!!.isChecked = false
                }
            }
        }

        mToggleButtonLog!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                log("Logging enabled")
                mSensorNetworkService?.enableLogging(true)
            } else {
                log("Logging disabled")
                mSensorNetworkService?.enableLogging(false)
            }
        }

        mButtonVisualizerCycling!!.setOnClickListener {
            val intent = Intent(this@CliActivity, CyclingActivity::class.java)
            startActivity(intent)
        }

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(mUsbReceiver, filter)
    }

    private val mSensorNetworkServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SensorNetworkService.ServiceBinder
            mSensorNetworkService = binder.getService()
            mSensorNetworkService!!.openDevice(mBaudrate)
            log(if (mSensorNetworkService!!.driverStatus.opened) "Sensor network connected" else "Unable to connect sensor network")
            mSensorNetworkService!!.fetchSchedulerInfo()

            if (mSensorNetworkService!!.driverStatus.started) {
                mSwitch!!.isChecked = true
            }
            toggleButtonText(mSensorNetworkService!!.driverStatus.opened)

            if (mSensorNetworkService != null) {
                mSensorNetworkServiceBound = true
            } else {

            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSensorData(message: SensorNetworkEvent.SensorData) {
        log(message.rawData)
        when(message.schedulerInfo?.schedulerInfoType) {
            SensorNetworkEvent.SchedulerInfoType.TIMER_SCALER ->
                    mTextViewScaler?.text = message.schedulerInfo?.timerScaler.toString()
            SensorNetworkEvent.SchedulerInfoType.DEVICE_MAP ->
                    mTextViewDevices?.text = message.schedulerInfo?.deviceMap?.
                            map { it.toString() }?.joinToString(",")
            SensorNetworkEvent.SchedulerInfoType.SCHEDULE -> {
                var i:Int = 0
                message.schedulerInfo?.schedule?.
                        map { mListSchedules[i++].text = it.map { it.toString()}.joinToString(",") }
            }
            SensorNetworkEvent.SchedulerInfoType.STARTED -> mSwitch!!.isChecked = true
            SensorNetworkEvent.SchedulerInfoType.STOPPED -> mSwitch!!.isChecked = false
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopSensorNetworkService()
        unregisterReceiver(mUsbReceiver)
    }

}
