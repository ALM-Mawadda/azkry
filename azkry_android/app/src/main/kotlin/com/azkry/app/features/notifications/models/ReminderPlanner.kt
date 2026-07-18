package com.azkry.app.features.notifications.models

import com.azkry.app.core.models.Prayer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class PlannedReminder(
    val kind: ReminderKind,
    val at: LocalDateTime,
    /** True for the heads-up fired [ReminderPlanner.PRE_ADHAN_MINUTES] before an adhan. */
    val isPreReminder: Boolean = false,
)

/**
 * Pure next-occurrence computation for the reminder chain: one alarm is
 * outstanding at a time; when it fires the receiver plans the next.
 */
object ReminderPlanner {
    /** Offset applied to Fajr/Asr for the morning/evening adhkar reminders. */
    const val ADHKAR_OFFSET_MINUTES = 30L

    /** Lead time of the optional pre-adhan heads-up. */
    const val PRE_ADHAN_MINUTES = 15L

    /**
     * Earliest reminder strictly after [now] among [enabled] kinds; when
     * [preAdhanEnabled], every enabled adhan also gets a heads-up
     * [PRE_ADHAN_MINUTES] earlier. [timesForDate] supplies the day's prayer
     * times; the search covers today and tomorrow, which always contains the
     * next occurrence.
     */
    fun nextReminder(
        now: LocalDateTime,
        enabled: Set<ReminderKind>,
        preAdhanEnabled: Boolean = false,
        timesForDate: (LocalDate) -> Map<Prayer, LocalTime>,
    ): PlannedReminder? {
        if (enabled.isEmpty()) return null
        val today = now.toLocalDate()
        return sequenceOf(today, today.plusDays(1))
            .flatMap { date ->
                val times = timesForDate(date)
                enabled.asSequence().flatMap { kind ->
                    val occurrence = occurrenceOn(kind, date, times)
                        ?: return@flatMap emptySequence<PlannedReminder>()
                    val events = mutableListOf(PlannedReminder(kind, occurrence))
                    if (preAdhanEnabled && kind.isAdhan) {
                        events += PlannedReminder(
                            kind = kind,
                            at = occurrence.minusMinutes(PRE_ADHAN_MINUTES),
                            isPreReminder = true,
                        )
                    }
                    events.asSequence()
                }
            }
            .filter { it.at.isAfter(now) }
            .minByOrNull { it.at }
    }

    private fun occurrenceOn(
        kind: ReminderKind,
        date: LocalDate,
        times: Map<Prayer, LocalTime>,
    ): LocalDateTime? {
        val anchor = when (kind) {
            ReminderKind.MorningAdhkar -> Prayer.Fajr
            ReminderKind.EveningAdhkar -> Prayer.Asr
            else -> kind.prayer
        } ?: return null
        val time = times[anchor] ?: return null
        val offset = if (kind.isAdhan) 0L else ADHKAR_OFFSET_MINUTES
        return LocalDateTime.of(date, time).plusMinutes(offset)
    }
}
