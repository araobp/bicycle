package jp.araobp.iot.cli.driver

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

abstract class SensorNetworkService: Service(), ISensorNetworkDriver {

    val mBinder: IBinder = ServiceBinder()

    val TAG_Parent = "SensorNetworkService"

    inner class ServiceBinder : Binder() {
        fun getService(): SensorNetworkService {
            return this@SensorNetworkService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG_Parent, "SensorNetworkService started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}