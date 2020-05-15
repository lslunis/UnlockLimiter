package net.lunis.unlocklimiter

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder


class ScreenLockStateService : Service() {
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> onScreenOn()
                    Intent.ACTION_SCREEN_OFF -> onScreenOff()
                }

            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun onScreenOn() {

    }

    fun onScreenOff() {

    }
}
