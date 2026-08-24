package com.shiftschedule.app.util

import java.time.LocalDate
import java.time.YearMonth

object RuHolidays {
    private val dates: Set<LocalDate> = buildSet {
        (1..8).forEach { add(LocalDate.of(2026, 1, it)) }
        add(LocalDate.of(2026, 2, 23))
        add(LocalDate.of(2026, 3, 8)); add(LocalDate.of(2026, 3, 9))
        add(LocalDate.of(2026, 5, 1)); add(LocalDate.of(2026, 5, 9)); add(LocalDate.of(2026, 5, 11))
        add(LocalDate.of(2026, 6, 12)); add(LocalDate.of(2026, 6, 14))
        add(LocalDate.of(2026, 11, 4))
        (1..8).forEach { add(LocalDate.of(2027, 1, it)) }
        add(LocalDate.of(2027, 2, 23))
        add(LocalDate.of(2027, 3, 8))
        add(LocalDate.of(2027, 5, 1)); add(LocalDate.of(2027, 5, 9)); add(LocalDate.of(2027, 5, 10))
        add(LocalDate.of(2027, 6, 12)); add(LocalDate.of(2027, 6, 14))
        add(LocalDate.of(2027, 11, 4))
    }
    fun isHoliday(d: LocalDate) = d in dates
    fun countInMonth(ym: YearMonth) = dates.count { YearMonth.from(it) == ym }
}
