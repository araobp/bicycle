package jp.araobp.iot.manager

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ToggleButton
import jp.araobp.iot.cli.CliActivity
import jp.araobp.iot.cli.CliService

import jp.araobp.iot.cli.R
import android.content.Context.BIND_AUTO_CREATE
import android.content.ServiceConnection
import android.os.IBinder
import jp.araobp.iot.cli.CliService.CliServiceBinder


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

    var mCliService: CliService? = null
    var mCliServiceBound = false

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

        val intent = Intent(this, CliService::class.java)
        bindService(intent, mCliServiceConnection, Context.BIND_AUTO_CREATE)

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

    private val mCliServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CliServiceBinder
            mCliService = binder.getService()
            mCliServiceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mCliServiceBound = false
        }
    }

}
