package net.lunis.unlocklimiter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.SystemClock.elapsedRealtime
import android.util.Log
import androidx.core.app.NotificationCompat


class ScreenLockStateService : Service() {
    private lateinit var receiver: BroadcastReceiver
    private var sessionPeriod: Long = 3_600_000
    private var restPeriod: Long = 300_000
    private var nagPeriod: Long = 15_000
    private var didLockAt: Long = 0
    private var shouldLockAt: Long = 0

    override fun onCreate() {
        Log.w("ScreenLockStateService", "On create")
        super.onCreate()
        val channel =
            NotificationChannel("nudges", "Nudges", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val foregroundNotification = NotificationCompat.Builder(this, "nudges")
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel).build()
        startForeground(1, foregroundNotification)
        // TODO: read *Period from storage
        receiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }

                when (intent.action) {
                    Intent.ACTION_USER_PRESENT -> onUnlock()
                    Intent.ACTION_SCREEN_OFF -> onLock()
                }

            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("get updated *Period settings from MainActivity")
    }

    fun onUnlock() {
        val now = elapsedRealtime()
        if (now - didLockAt >= restPeriod) {
            shouldLockAt = now + sessionPeriod
        }
        if (now < shouldLockAt) {
            nagAt(shouldLockAt)
        } else {
            nag()
        }
    }

    fun onLock() {
        didLockAt = elapsedRealtime()
        // TODO: clear nag handler
    }

    fun nag() {
        val now = elapsedRealtime()
        // TODO: notify {now - shouldLockAt} excess
        nagAt(now + nagPeriod)
    }

    fun nagAt(time: Long) {
        // TODO: set nag handler
    }
}
