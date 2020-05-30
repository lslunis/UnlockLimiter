package net.lunis.unlocklimiter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock.elapsedRealtime
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class ScreenLockStateService : Service() {
    private val foregroundId: Int = 1
    private val nudgeId: Int = 2
    private lateinit var handler: Handler
    private lateinit var receiver: BroadcastReceiver
    private var sessionPeriod: Long = 600_000
    private var restPeriod: Long = 300_000
    private var nudgePeriod: Long = 10_000
    private var didLockAt: Long = 0
    private var shouldLockAt: Long = 0

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())

        val nm: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(
                    "foreground",
                    "Unlock Limiter",
                    NotificationManager.IMPORTANCE_LOW
                ),

                NotificationChannel(
                    "nudge",
                    "Nudges",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        )
        val foregroundNotification = NotificationCompat.Builder(this, "foreground")
            .setSmallIcon(R.drawable.session)
            .build()
        startForeground(foregroundId, foregroundNotification)

        // TODO: read *Period from storage
        receiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if (intent == null) return
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
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isInteractive) onUnlock() else onLock()
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
        if (shouldLockAt == 0L || now - didLockAt >= restPeriod) {
            shouldLockAt = now + sessionPeriod
            NotificationManagerCompat.from(this).cancel(nudgeId)
        }
        val nudgeDelay = shouldLockAt - now
        if (nudgeDelay > 0) nudgeAfter(nudgeDelay) else nudge()

    }

    fun onLock() {
        didLockAt = elapsedRealtime()
        handler.removeCallbacksAndMessages(null)
    }

    fun nudge() {
        val now = elapsedRealtime()
        NotificationManagerCompat.from(this).notify(
            nudgeId,
            NotificationCompat.Builder(this, "nudge")
                .setSmallIcon(R.drawable.rest)
                .setContentText("Seconds over limit: " + ((now - shouldLockAt) / 1000))
                .build()
        )
        nudgeAfter(nudgePeriod)
    }

    fun nudgeAfter(delay: Long) {
        handler.postDelayed(Runnable { nudge() }, delay)
    }
}
