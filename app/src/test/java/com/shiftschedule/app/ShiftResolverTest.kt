package com.shiftschedule.app

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.domain.ShiftResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ShiftResolverTest {
    private val template = Template(1, "2-2-2", "", "D,D,N,N,O,O", true)

    private fun schedule(
        start: String = "2028-02-27",
        exceptions: Map<String, String> = emptyMap(),
        cycles: Map<String, Int> = emptyMap()
    ) = Schedule(
        id = 1,
        name = "Test",
        color = "#5856D6",
        templateId = 1,
        startDate = start,
        exceptions = exceptions,
        cycleShifts = cycles
    )

    @Test
    fun `pattern repeats across leap day`() {
        val s = schedule()
        assertEquals("D", ShiftResolver.resolve(s, LocalDate.of(2028, 2, 27), template)?.code)
        assertEquals("D", ShiftResolver.resolve(s, LocalDate.of(2028, 2, 28), template)?.code)
        assertEquals("N", ShiftResolver.resolve(s, LocalDate.of(2028, 2, 29), template)?.code)
        assertEquals("N", ShiftResolver.resolve(s, LocalDate.of(2028, 3, 1), template)?.code)
    }

    @Test
    fun `date before schedule start returns null`() {
        assertNull(ShiftResolver.resolve(schedule("2028-02-28"), LocalDate.of(2028, 2, 27), template))
    }

    @Test
    fun `valid exception overrides pattern`() {
        val s = schedule(exceptions = mapOf("2028-02-28" to "V"))
        assertEquals("V", ShiftResolver.resolve(s, LocalDate.of(2028, 2, 28), template)?.code)
    }

    @Test
    fun `invalid exception does not hide valid pattern`() {
        val s = schedule(exceptions = mapOf("2028-02-28" to "UNKNOWN"))
        assertEquals("D", ShiftResolver.resolve(s, LocalDate.of(2028, 2, 28), template)?.code)
    }

    @Test
    fun `invalid schedule date returns null`() {
        val s = schedule(start = "not-a-date")
        assertNull(ShiftResolver.resolve(s, LocalDate.of(2028, 2, 28), template))
    }

    @Test
    fun `multiple cycle shifts are applied independently and deterministically`() {
        val s = schedule(
            start = "2026-08-01",
            cycles = mapOf(
                "2026-08-10" to 3,
                "2026-08-05" to 2
            )
        )
        // 15 Aug: base index 14, both completed periods consume 5 days -> index 9 -> N.
        assertEquals("N", ShiftResolver.resolve(s, LocalDate.of(2026, 8, 15), template)?.code)
    }

    @Test
    fun `invalid and non-positive cycle entries are ignored`() {
        val s = schedule(
            start = "2026-08-01",
            cycles = mapOf("bad-date" to 10, "2026-08-05" to 0)
        )
        assertEquals("N", ShiftResolver.resolve(s, LocalDate.of(2026, 8, 4), template)?.code)
    }
    @Test
    fun `cycle before schedule start is ignored`() {
        val s = schedule(
            start = "2026-08-10",
            cycles = mapOf("2026-08-01" to 5)
        )
        assertEquals("D", ShiftResolver.resolve(s, LocalDate.of(2026, 8, 10), template)?.code)
    }

    @Test
    fun `overlapping cycle ranges do not double count days`() {
        val s = schedule(
            start = "2026-08-01",
            cycles = mapOf("2026-08-05" to 5, "2026-08-08" to 5)
        )
        // 15 Aug: base index 14, unique consumed period is 8 days -> index 6 -> D.
        assertEquals("D", ShiftResolver.resolve(s, LocalDate.of(2026, 8, 15), template)?.code)
    }

}
