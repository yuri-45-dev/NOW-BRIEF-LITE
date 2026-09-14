package com.nowbrief.livecapsule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Owns the single ongoing "Now Brief" notification — this is the thing the
 * OS itself promotes into the Now Bar / Live Update chip. We do NOT draw the
 * pill pixel-by-pixel here: Android 16's promoted-ongoing surface explicitly
 * forbids custom RemoteViews, so the gradient capsule you see on the lock
 * screen / status bar (matching the reference screenshots) is rendered by
 * the system itself from setSmallIcon() + setColor() + the style below. Our
 * job is only to (a) qualify for promotion and (b) supply the right icon,
 * accent color and text — the pill/capsule visual on the *home screen* is
 * the separate widget in widget/NowBriefWidgetProvider.kt, which we do
 * fully control.
 *
 * Promotion eligibility (this is what was missing before, and why the app
 * never showed up under Settings > Notifications > Live notifications):
 *  1. The notification MUST use one of BigTextStyle / CallStyle / ProgressStyle
 *     — a bare Builder with no .setStyle() at all is never eligible. We use
 *     BigTextStyle since we have a real discrete status, not a measurable
 *     progress value.
 *  2. setOngoing(true) — has to represent a running/ongoing state.
 *  3. The channel must be at least IMPORTANCE_DEFAULT — IMPORTANCE_LOW
 *     notifications are not promoted.
 *  4. The android.requestPromotedOngoing extra must be set to true.
 *  5. POST_PROMOTED_NOTIFICATIONS + POST_NOTIFICATIONS must both be granted.
 */
object NowBriefNotifier {

    private const val CHANNEL_ID = "now_brief_live_channel"
    private const val NOTIFICATION_ID = 7801

    // Extra key the platform reads to request Live Update promotion.
    // (A typed NotificationCompat.Builder.setRequestPromotedOngoing() exists on newer
    // androidx.core releases; this raw extra is the underlying mechanism and compiles
    // against any androidx.core version at compileSdk 36.)
    private const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        // IMPORTANCE_DEFAULT (not LOW): Live Update promotion requires at least default
        // importance. If this channel was already created by an earlier build of the app
        // at IMPORTANCE_LOW, delete+recreate it, since importance can't be raised in place.
        if (existing != null && existing.importance < NotificationManager.IMPORTANCE_DEFAULT) {
            manager.deleteNotificationChannel(CHANNEL_ID)
        }
        if (existing == null || existing.importance < NotificationManager.IMPORTANCE_DEFAULT) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_now_brief_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_now_brief_description)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** Rebuilds and (re)posts the notification for whatever period "now" is in. */
    fun updateNow(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        ensureChannel(context)

        val period = DayPeriod.forCalendar()
        val label = context.getString(period.labelResId)
        val accent = ContextCompat.getColor(context, period.accentColorRes())

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_now_brief)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(label))
            .setContentIntent(openAppIntent)
            .setOngoing(true)                 // required: must be an ongoing event
            .setOnlyAlertOnce(true)            // silent refreshes — no repeated pings
            .setColor(accent)
            .setColorized(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= 36) {
            builder.extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }
}

private fun DayPeriod.accentColorRes(): Int = when (this) {
    DayPeriod.MORNING -> R.color.period_morning_accent
    DayPeriod.DAY -> R.color.period_day_accent
    DayPeriod.EVENING -> R.color.period_evening_accent
    DayPeriod.NIGHT -> R.color.period_night_accent
}
