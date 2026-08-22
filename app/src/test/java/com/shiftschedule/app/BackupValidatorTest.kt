package com.shiftschedule.app

import com.shiftschedule.app.data.model.BackupData
import com.shiftschedule.app.data.model.BackupValidator
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BackupValidatorTest {
    private val template = Template(10, "Custom", "", "D,N,O")
    private val schedule = Schedule(
        id = 10,
        name = "Work",
        color = "#5856D6",
        templateId = 10,
        startDate = "2026-08-01"
    )

    @Test
    fun `valid backup passes`() {
        assertNull(BackupValidator.validate(BackupData(2, listOf(schedule), listOf(template))))
    }

    @Test
    fun `unsupported version fails`() {
        assertNotNull(BackupValidator.validate(BackupData(99, listOf(schedule), listOf(template))))
    }

    @Test
    fun `invalid shift exception fails`() {
        val bad = schedule.copy(exceptions = mapOf("2026-08-01" to "X"))
        assertNotNull(BackupValidator.validate(BackupData(2, listOf(bad), listOf(template))))
    }

    @Test
    fun `invalid color fails`() {
        val bad = schedule.copy(color = "not-a-color")
        assertNotNull(BackupValidator.validate(BackupData(2, listOf(bad), listOf(template))))
    }

    @Test
    fun `missing custom template fails`() {
        val bad = schedule.copy(templateId = 999)
        assertNotNull(BackupValidator.validate(BackupData(2, listOf(bad), listOf(template))))
    }
}
