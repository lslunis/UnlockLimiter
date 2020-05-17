package net.lunis.unlocklimiter

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log


class ScreenLockStateService : Service() {
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }
                Log.w("ScreenLockStateService", intent.action)
//                when (intent.action) {
//                    Intent.ACTION_SCREEN_ON -> onScreenOn()
//                    Intent.ACTION_SCREEN_OFF -> onScreenOff()
//                }

            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_USER_UNLOCKED)
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
