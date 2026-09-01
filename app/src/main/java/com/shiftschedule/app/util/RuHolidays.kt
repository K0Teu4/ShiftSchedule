package com.shiftschedule.app.util

import java.time.LocalDate
import java.time.YearMonth

object RuHolidays {
    private val dates: Set<LocalDate> = buildSet {
        for (year in 2026..2030) {
            (1..8).forEach { add(LocalDate.of(year, 1, it)) }
            add(LocalDate.of(year, 2, 23))
            add(LocalDate.of(year, 3, 8))
            add(LocalDate.of(year, 5, 1))
            add(LocalDate.of(year, 5, 9))
            add(LocalDate.of(year, 6, 12))
            add(LocalDate.of(year, 11, 4))
        }
    }

    fun isHoliday(d: LocalDate) = d in dates
    fun countInMonth(ym: YearMonth) = dates.count { YearMonth.from(it) == ym }
}
