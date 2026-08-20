package com.shiftschedule.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StringsTest {
    @Test
    fun ruAndEnSameKeys() {
        val cls = Strings::class.java
        val ru = (cls.getDeclaredField("ru").apply { isAccessible = true }.get(Strings) as Map<*, *>).keys
        val en = (cls.getDeclaredField("en").apply { isAccessible = true }.get(Strings) as Map<*, *>).keys
        assertEquals(ru, en)
    }

    @Test
    fun allKeysResolved() {
        val keys = listOf("today_label", "shared_off", "find_star", "no_shared", "try_other",
            "total_sick", "total_vacation", "theme_ocean", "theme_forest", "theme_berry",
            "theme_sand", "theme_plum", "theme_graphite", "settings_controls_title",
            "settings_controls_desc", "app_info_title", "app_info_desc", "app_license",
            "ctrl_tap", "ctrl_long", "ctrl_swipe", "ctrl_drag", "ctrl_copy", "ctrl_compare",
            "about_1", "about_2", "about_3", "about_4", "about_5")
        for (k in keys) for (lang in listOf("ru", "en")) assertNotEquals(k, Strings.raw(lang, k))
    }
}
