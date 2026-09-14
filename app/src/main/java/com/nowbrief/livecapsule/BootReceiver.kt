package com.nowbrief.livecapsule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * A device reboot clears all AlarmManager alarms, so we re-arm the single
 * next boundary alarm once here and go straight back to sleep.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            PeriodAlarmScheduler.scheduleNextBoundary(context)
            NowBriefNotifier.updateNow(context)
        }
    }
}
