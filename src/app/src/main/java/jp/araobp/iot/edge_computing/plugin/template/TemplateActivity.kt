package jp.araobp.iot.edge_computing.plugin.template

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import jp.araobp.iot.cli.R
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus

class TemplateActivity : Activity() {

    companion object {
        private val TAG = javaClass.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        actionBar.setDisplayHomeAsUpEnabled(true)
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
    fun onProcessedData(processedData: Template.ProcessedData) {
        Log.d(TAG, processedData.toString())
        when(processedData.deviceId) {
            // TODO: implementation
        }
    }
}