package jp.araobp.iot.manager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ToggleButton
import jp.araobp.iot.cli.CliActivity
import jp.araobp.iot.cli.CliService

import jp.araobp.iot.cli.R

class ManagerActivity : Activity() {

    val TAG = "Manager"

    var mButtonCli: Button? = null
    var mButtonBuiltinSensors: Button? = null
    var mButtonEdgeComputing: Button? = null
    var mButtonVisualizer: Button? = null
    var mButtonDbClient: Button? = null

    var mToggleButtonCli: ToggleButton? = null
    var mToggleButtonBuiltinSensors: ToggleButton? = null
    var mToggleButtonEdgeComputing: ToggleButton? = null
    var mToggleButtonVisualizer: ToggleButton? = null
    var mToggleButtonDbClient: ToggleButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        mButtonCli = findViewById(R.id.buttonCli) as Button
        mButtonBuiltinSensors = findViewById(R.id.buttonBuiltinSensors) as Button
        mButtonEdgeComputing = findViewById(R.id.buttonEdgeComputing) as Button
        mButtonVisualizer = findViewById(R.id.buttonVisualizer) as Button
        mButtonDbClient = findViewById(R.id.buttonDbClient) as Button

        mToggleButtonCli = findViewById(R.id.toggleButtonCli) as ToggleButton
        mToggleButtonBuiltinSensors = findViewById(R.id.toggleButtonBuiltinSensors) as ToggleButton
        mToggleButtonEdgeComputing = findViewById(R.id.toggleButtonEdgeComputing) as ToggleButton
        mToggleButtonVisualizer = findViewById(R.id.toggleButtonVisualizer) as ToggleButton
        mToggleButtonDbClient = findViewById(R.id.toggleButtonDbClient) as ToggleButton

        mToggleButtonCli!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {  // OFF
                Log.d(TAG, "CLI Off")
            } else {  // ON
                Log.d(TAG, "CLI On")
                var intent = Intent(this, CliService::class.java)
                startService(intent)
            }
        }

        mButtonCli!!.setOnClickListener {
            Log.d(TAG, "start CLI UI")
            var intent = Intent(this, CliActivity::class.java)
            startActivity(intent)
        }
    }
}
