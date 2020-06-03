package net.lunis.unlocklimiter

import android.app.KeyguardManager
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
import android.os.SystemClock.elapsedRealtime
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
    private var restStart: Long = 0
    private var sessionEnd: Long = 0

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
                if (intent != null) onChange()
            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(receiver, filter)
        onChange()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("get updated *Period settings from MainActivity")
    }

    fun onChange() {
        val now = elapsedRealtime()
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (km.isKeyguardLocked) {
            if (restStart == 0L) restStart = now
            handler.removeCallbacksAndMessages(null)

        } else {
            if (sessionEnd == 0L || restStart != 0L && now - restStart >= restPeriod) {
                sessionEnd = now + sessionPeriod
                NotificationManagerCompat.from(this).cancel(nudgeId)
            }
            restStart = 0
            val nudgeDelay = sessionEnd - now
            if (nudgeDelay > 0) nudgeAfter(nudgeDelay) else nudge()
        }
    }

    fun nudge() {
        val now = elapsedRealtime()
        NotificationManagerCompat.from(this).notify(
            nudgeId,
            NotificationCompat.Builder(this, "nudge")
                .setSmallIcon(R.drawable.rest)
                .setContentText("Seconds over limit: " + ((now - sessionEnd) / 1000))
                .build()
        )
        nudgeAfter(nudgePeriod)
    }

    fun nudgeAfter(delay: Long) {
        handler.postDelayed(Runnable { nudge() }, delay)
    }
}
