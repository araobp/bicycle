package jp.araobp.iot.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import jp.araobp.iot.sensor_network.SensorNetworkEvent
import jp.araobp.iot.sensor_network.SensorNetworkProtocol
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus



class CyclingActivity : Activity() {

    val TAG = "CyclingActivity"

    var mTextViewAccelValueX: TextView? = null
    var mTextViewAccelValueY: TextView? = null
    var mTextViewAccelValueZ: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycling)
        actionBar.setDisplayHomeAsUpEnabled(true)

        mTextViewAccelValueX = findViewById(R.id.textViewAccelValueX) as TextView
        mTextViewAccelValueY = findViewById(R.id.textViewAccelValueY) as TextView
        mTextViewAccelValueZ = findViewById(R.id.textViewAccelValueZ) as TextView
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
    fun onProcessedData(processedData: SensorNetworkEvent.ProcessedData) {
        Log.d(TAG, processedData.toString())
        when(processedData.deviceId) {
            SensorNetworkProtocol.KXR94_2050 -> {
                var accel: List<Any>? = processedData.data
                if (accel != null) {
                    mTextViewAccelValueX?.text = accel[0].toString()
                    mTextViewAccelValueY?.text = accel[1].toString()
                    mTextViewAccelValueZ?.text = accel[2].toString()
                }
            }
        }
    }
}