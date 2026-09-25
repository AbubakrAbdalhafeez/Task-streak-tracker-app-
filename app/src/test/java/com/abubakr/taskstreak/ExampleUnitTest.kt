package com.abubakr.taskstreak

import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.StreakCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun testStreakCalculationWithTodayCompleted() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val task = TaskEntity(
            id = 1L,
            title = "Read Quran & Books",
            category = "Personal",
            categoryColorHex = "#FF5722",
            recurrenceType = RecurrenceType.DAILY.name,
            startDate = DateUtils.format(twoDaysAgo)
        )

        val completions = setOf(
            DateUtils.format(today),
            DateUtils.format(yesterday),
            DateUtils.format(twoDaysAgo)
        )

        val stats = StreakCalculator.calculateTaskStats(task, completions, today)

        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
        assertTrue(stats.isCompletedToday)
        assertEquals(100f, stats.completionRate)
    }

    @Test
    fun testGracePeriodWhenTodayNotYetCompleted() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val task = TaskEntity(
            id = 2L,
            title = "Workout",
            category = "Fitness",
            categoryColorHex = "#10B981",
            recurrenceType = RecurrenceType.DAILY.name,
            startDate = DateUtils.format(twoDaysAgo)
        )

        // Yesterday and two days ago are completed, today is still pending
        val completions = setOf(
            DateUtils.format(yesterday),
            DateUtils.format(twoDaysAgo)
        )

        val stats = StreakCalculator.calculateTaskStats(task, completions, today)

        // Because of grace period, the streak is NOT broken today; it stays 2
        assertEquals(2, stats.currentStreak)
        assertFalse(stats.isCompletedToday)
    }

    @Test
    fun testBrokenStreakWhenYesterdayMissed() {
        val today = LocalDate.now()
        val twoDaysAgo = today.minusDays(2)

        val task = TaskEntity(
            id = 3L,
            title = "Study English",
            category = "Study",
            categoryColorHex = "#3B82F6",
            recurrenceType = RecurrenceType.DAILY.name,
            startDate = DateUtils.format(twoDaysAgo)
        )

        // Only two days ago was done; yesterday was missed, today not done
        val completions = setOf(
            DateUtils.format(twoDaysAgo)
        )

        val stats = StreakCalculator.calculateTaskStats(task, completions, today)

        // Yesterday was missed, streak is 0
        assertEquals(0, stats.currentStreak)
    }
}
