package jp.araobp.iot.ui

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import jp.araobp.iot.sensor_network.SensorDataHandlerActivity
import jp.araobp.iot.sensor_network.SensorNetworkService

class CyclingActivity : SensorDataHandlerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycling_visualizer)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item!!.itemId) {
            android.R.id.home -> this.finish()
        }
        return true
    }

    override fun onSensorData(sensorData: SensorNetworkService.SensorData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}