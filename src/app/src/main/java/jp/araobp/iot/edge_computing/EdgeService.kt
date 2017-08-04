package jp.araobp.iot.edge_computing

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class EdgeService : Service() {

    val mBinder:IBinder = EdgeServiceBinder()

    inner class EdgeServiceBinder : Binder() {
        fun getService(): EdgeService {
            return this@EdgeService
        }
    }

    val TAG = "EdgeService"

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    fun test(message: String) {
        Log.d(TAG, message)
    }
}
