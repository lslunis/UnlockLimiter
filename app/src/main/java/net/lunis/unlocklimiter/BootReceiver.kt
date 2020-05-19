package net.lunis.unlocklimiter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, ScreenLockStateService::class.java))
    }
}
