package com.nowbrief.livecapsule.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.nowbrief.livecapsule.DayPeriod
import com.nowbrief.livecapsule.R
import com.nowbrief.livecapsule.pillBackgroundRes

/**
 * Renders now_pill_layout.xml as a home-screen widget. Its manifest entry
 * intentionally omits `android:updatePeriodMillis`, so the OS never wakes
 * this provider on its own — the only trigger is [requestUpdate], called
 * once per boundary from PeriodAlarmReceiver.
 */
class NowBriefWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id -> pushViews(context, appWidgetManager, id) }
    }

    companion object {

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, NowBriefWidgetProvider::class.java)
            )
            ids.forEach { id -> pushViews(context, manager, id) }
        }

        private fun pushViews(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val period = DayPeriod.forCalendar()
            val views = RemoteViews(context.packageName, R.layout.now_pill_layout)
            views.setTextViewText(R.id.now_pill_text, context.getString(period.labelResId))
            views.setInt(
                R.id.now_pill_root,
                "setBackgroundResource",
                period.pillBackgroundRes()
            )
            manager.updateAppWidget(widgetId, views)
        }
    }
}
