package com.shiftschedule.app

import android.app.Application
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.repository.ShiftRepository
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.StatsUtils
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ShiftViewModelTest {
    private lateinit var dao: FakeShiftDao
    private lateinit var repository: ShiftRepository
    private lateinit var viewModel: ShiftViewModel
    private val app: Application = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao = FakeShiftDao()
        repository = ShiftRepository(dao)
        viewModel = ShiftViewModel(app, repository, FakeSettingsStore())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addSchedule trims name and sets sortIndex`() = runTest {
        viewModel.addSchedule(Schedule(0, "  Я  ", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 0))
        val list = repository.allSchedules.first()
        assertEquals(1, list.size)
        assertEquals("Я", list[0].name)
        assertEquals(1, list[0].sortIndex)
    }

    @Test
    fun `addSchedule rejects blank name`() = runTest {
        viewModel.addSchedule(Schedule(0, "   ", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 0))
        assertEquals(0, repository.allSchedules.first().size)
    }

    @Test
    fun `duplicate creates copy with suffix`() = runTest {
        viewModel.addSchedule(Schedule(0, "Я", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 0))
        val original = repository.allSchedules.first().first()
        viewModel.duplicateSchedule(original)
        val list = repository.allSchedules.first()
        assertEquals(2, list.size)
        assertTrue(list.any { it.name == "Я (копия)" })
    }

    @Test
    fun `reorder persists sortIndex`() = runTest {
        val s1 = Schedule(0, "A", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 0)
        val s2 = Schedule(0, "B", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 1)
        val s3 = Schedule(0, "C", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 2)

        dao.insertSchedule(s1)
        dao.insertSchedule(s2)
        dao.insertSchedule(s3)

        val job = launch { viewModel.allSchedules.collect {} }
        advanceUntilIdle()

        viewModel.reorderSchedules(0, 2)
        advanceUntilIdle()

        val after = repository.allSchedules.first()
        assertEquals("A", after[2].name)
        assertEquals(2, after[2].sortIndex)
        assertEquals("B", after[0].name)
        assertEquals(0, after[0].sortIndex)

        job.cancel()
    }

    @Test
    fun `delete removes schedule`() = runTest {
        viewModel.addSchedule(Schedule(0, "A", "#1", null, "2026-08-01", true, emptyMap(), emptyMap(), 0))
        val s = repository.allSchedules.first().first()
        viewModel.deleteSchedule(s)
        assertEquals(0, repository.allSchedules.first().size)
    }

    @Test
    fun `monthStats calculates correctly`() = runTest {
        // Тестируем чистую логику статистики напрямую — это стабильнее, чем через StateFlow
        val template = com.shiftschedule.app.data.model.Template(1, "D/O", "", "D,O", true)
        val schedule = Schedule(1, "A", "#1", 1, "2026-08-01", true, emptyMap(), emptyMap(), 0)

        val stats = StatsUtils.monthStats(
            listOf(schedule),
            mapOf(1 to template),
            listOf(1),
            YearMonth.of(2026, 8)
        )

        assertEquals(16, stats["total_day"])
        assertEquals(15, stats["total_off"])
    }
}