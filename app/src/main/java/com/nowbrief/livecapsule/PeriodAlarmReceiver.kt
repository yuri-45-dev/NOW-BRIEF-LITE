package com.nowbrief.livecapsule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nowbrief.livecapsule.widget.NowBriefWidgetProvider

/**
 * Wakes up exactly at each of the 4 daily boundaries, does one small unit of
 * work (refresh the Live Update notification + the home-screen capsule
 * widget), re-arms the single next alarm, and returns. No loops, no polling.
 */
class PeriodAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        try {
            NowBriefNotifier.updateNow(context)
            NowBriefWidgetProvider.requestUpdate(context)
            PeriodAlarmScheduler.scheduleNextBoundary(context)
        } finally {
            pendingResult.finish()
        }
    }
}
