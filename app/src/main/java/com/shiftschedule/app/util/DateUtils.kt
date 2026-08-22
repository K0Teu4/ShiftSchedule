package com.shiftschedule.app.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun parseDate(dateString: String): LocalDate = LocalDate.parse(dateString, dateFormatter)

    fun tryParseDate(dateString: String): LocalDate? = runCatching {
        LocalDate.parse(dateString, dateFormatter)
    }.getOrNull()

    fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate> {
        return (1..yearMonth.lengthOfMonth()).map { day -> yearMonth.atDay(day) }
    }

    fun getFirstDayOffset(yearMonth: YearMonth, weekStart: String): Int {
        val dayOfWeek = yearMonth.atDay(1).dayOfWeek.value
        return if (weekStart == "mon") (dayOfWeek - 1) % 7 else dayOfWeek % 7
    }

    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun monthName(yearMonth: YearMonth): String {
        val raw = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        return raw.replaceFirstChar { if (it.isLowerCase()) it.uppercase(Locale("ru")) else it.toString() }
    }

    fun monthTitle(yearMonth: YearMonth): String {
        return monthName(yearMonth) + " " + yearMonth.year
    }

    fun monthName(yearMonth: YearMonth, locale: Locale): String {
        val raw = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        return raw.replaceFirstChar { if (it.isLowerCase()) it.uppercase(locale) else it.toString() }
    }

    fun monthTitle(yearMonth: YearMonth, locale: Locale): String {
        return monthName(yearMonth, locale) + " " + yearMonth.year
    }

    fun weekDayHeaders(weekStart: String, lang: String): List<String> {
        if (lang == "en") {
            return if (weekStart == "mon") {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            } else {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            }
        }
        return weekDayHeaders(weekStart)
    }

    fun weekDayHeaders(weekStart: String): List<String> {
        return if (weekStart == "mon") {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        } else {
            listOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        }
    }
}