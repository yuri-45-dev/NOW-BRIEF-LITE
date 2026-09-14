package com.nowbrief.livecapsule

import java.util.Calendar

/**
 * The four "Now Brief" periods of the day and the local boundary logic
 * that drives them. Nothing here touches a clock loop or a coroutine —
 * it is pure, cheap, and only ever called from an alarm/broadcast callback.
 */
enum class DayPeriod(
    val labelResId: Int,
    val startHour: Int,
    val startMinute: Int
) {
    // 06:00 – 11:59
    MORNING(R.string.period_morning, 6, 0),

    // 12:00 – 16:59
    DAY(R.string.period_day, 12, 0),

    // 17:00 – 19:59
    EVENING(R.string.period_evening, 17, 0),

    // 20:00 – 05:59 (wraps past midnight)
    NIGHT(R.string.period_night, 20, 0);

    companion object {

        /** Resolves which period "now" (or [calendar]) falls into. */
        fun forCalendar(calendar: Calendar = Calendar.getInstance()): DayPeriod {
            val minutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                calendar.get(Calendar.MINUTE)
            return when {
                minutesOfDay >= 6 * 60 && minutesOfDay < 12 * 60 -> MORNING
                minutesOfDay >= 12 * 60 && minutesOfDay < 17 * 60 -> DAY
                minutesOfDay >= 17 * 60 && minutesOfDay < 20 * 60 -> EVENING
                else -> NIGHT
            }
        }

        /**
         * Epoch millis of the *next* period boundary strictly after [from].
         * Used to arm exactly one AlarmManager alarm — never a repeating
         * or polling timer.
         */
        fun nextBoundaryMillis(from: Calendar = Calendar.getInstance()): Long {
            val boundaries = listOf(6, 12, 17, 20)
            val candidate = from.clone() as Calendar
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)

            for (hour in boundaries) {
                candidate.set(Calendar.HOUR_OF_DAY, hour)
                candidate.set(Calendar.MINUTE, 0)
                if (candidate.timeInMillis > from.timeInMillis) {
                    return candidate.timeInMillis
                }
            }
            // All of today's boundaries have passed — next one is 06:00 tomorrow.
            candidate.set(Calendar.HOUR_OF_DAY, 6)
            candidate.set(Calendar.MINUTE, 0)
            candidate.add(Calendar.DAY_OF_YEAR, 1)
            return candidate.timeInMillis
        }
    }
}

/** Home-screen widget pill background for this period — shared by MainActivity's
 *  in-app preview and NowBriefWidgetProvider so both always agree visually. */
fun DayPeriod.pillBackgroundRes(): Int = when (this) {
    DayPeriod.MORNING -> R.drawable.pill_background_morning
    DayPeriod.DAY -> R.drawable.pill_background_day
    DayPeriod.EVENING -> R.drawable.pill_background_evening
    DayPeriod.NIGHT -> R.drawable.pill_background_night
}
