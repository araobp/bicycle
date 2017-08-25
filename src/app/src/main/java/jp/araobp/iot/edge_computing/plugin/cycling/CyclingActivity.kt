package jp.araobp.iot.edge_computing.plugin.cycling

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import jp.araobp.iot.cli.R
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus

class CyclingActivity : Activity() {

    companion object {
        private val TAG = "CyclingActivity"
    }

    var mTextViewSpeedValue: TextView? = null
    var mTextViewRpmValue: TextView? = null

    var mTextViewTempValue: TextView? = null
    var mTextViewHumidValue: TextView? = null

    var mTextViewAccelValueX: TextView? = null
    var mTextViewAccelValueY: TextView? = null
    var mTextViewAccelValueZ: TextView? = null

    var mTextViewLatitudeValue: TextView? = null
    var mTextViewLongitudeValue: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycling)
        actionBar.setDisplayHomeAsUpEnabled(true)

        mTextViewSpeedValue = findViewById(R.id.textViewSpeedValue) as TextView
        mTextViewRpmValue = findViewById(R.id.textViewRpmValue) as TextView

        mTextViewHumidValue = findViewById(R.id.textViewHumidValue) as TextView
        mTextViewTempValue = findViewById(R.id.textViewTempValue) as TextView

        mTextViewAccelValueX = findViewById(R.id.textViewAccelValueX) as TextView
        mTextViewAccelValueY = findViewById(R.id.textViewAccelValueY) as TextView
        mTextViewAccelValueZ = findViewById(R.id.textViewAccelValueZ) as TextView

        mTextViewLatitudeValue = findViewById(R.id.textViewLatitudeValue) as TextView
        mTextViewLongitudeValue = findViewById(R.id.textViewLogitudeValue) as TextView

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item!!.itemId) {
            android.R.id.home -> this.finish()
        }
        return true
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
    fun onProcessedData(processedData: Cycling.ProcessedData) {
        Log.d(TAG, processedData.toString())
        when(processedData.deviceId) {
            SensorNetworkProtocol.A1324LUA_T -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewRpmValue?.text = data[0].toString()
                    mTextViewSpeedValue?.text = data[1].toString()
                }
            }
            SensorNetworkProtocol.KXR94_2050 -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewAccelValueX?.text = data[0].toString()
                    mTextViewAccelValueY?.text = data[1].toString()
                    mTextViewAccelValueZ?.text = data[2].toString()
                }
            }
            SensorNetworkProtocol.AMBIENT_TEMPERATURE -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewTempValue?.text = data[0].toString()
                }
            }
            SensorNetworkProtocol.RELATIVE_HUMIDITY -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewHumidValue?.text = data[0].toString()
                }
            }
            SensorNetworkProtocol.HDC1000, SensorNetworkProtocol.SHT31_DIS -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewTempValue?.text = data[0].toString()
                    mTextViewHumidValue?.text = data[1].toString()
                }
            }
            SensorNetworkProtocol.ACCELEROMETER -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewAccelValueX?.text = data[0].toString()
                    mTextViewAccelValueY?.text = data[1].toString()
                    mTextViewAccelValueZ?.text = data[2].toString()
                }
            }
            SensorNetworkProtocol.FUSED_LOCATION -> {
                var data: List<Any?>? = processedData.data
                if (data != null) {
                    mTextViewLatitudeValue?.text = data[0].toString()
                    mTextViewLongitudeValue?.text = data[1].toString()
                }
            }
        }
    }
}