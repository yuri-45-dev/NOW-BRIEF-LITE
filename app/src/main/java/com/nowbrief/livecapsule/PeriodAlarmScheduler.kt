package com.nowbrief.livecapsule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Zero-background-drain scheduling core.
 *
 * There is no Service, no WorkManager periodic job, and no `while(true)` loop
 * anywhere in this app. The *only* thing that ever wakes the process up is a
 * single [AlarmManager.setExactAndAllowWhileIdle] fired at the next 06:00 /
 * 12:00 / 17:00 / 20:00 boundary. [PeriodAlarmReceiver] handles that wakeup,
 * updates the notification/widget text, then immediately re-arms the *next*
 * single alarm and the process goes back to sleep — never more than 4 wakeups
 * a day, each doing O(1) work.
 */
object PeriodAlarmScheduler {

    private const val REQUEST_CODE = 4200

    fun scheduleNextBoundary(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = DayPeriod.nextBoundaryMillis()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, PeriodAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any stale alarm first so we never stack duplicates.
        alarmManager.cancel(pendingIntent)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }
}
