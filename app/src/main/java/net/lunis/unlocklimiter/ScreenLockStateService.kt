package net.lunis.unlocklimiter

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.SystemClock.elapsedRealtime
import android.util.Log


class ScreenLockStateService : Service() {
    private lateinit var receiver: BroadcastReceiver
    private var sessionPeriod: Long = 3_600_000
    private var restPeriod: Long = 300_000
    private var nagPeriod: Long = 15_000
    private var didLockAt: Long = 0
    private var shouldLockAt: Long = 0

    override fun onCreate() {
        super.onCreate()
        // TODO: startForeground(1, foregroundNotification)
        // TODO: read *Period from storage
        receiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }

                // TODO: remove after choosing the most appropriate lock/unlock intents
                Log.w("ScreenLockStateService", intent.action)

                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> onUnlock()
                    Intent.ACTION_SCREEN_OFF -> onLock()
                }

            }
        }
        // TODO: prune list after choosing the most appropriate lock/unlock intents
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
        // TODO: get updated *Period settings from MainActivity
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
