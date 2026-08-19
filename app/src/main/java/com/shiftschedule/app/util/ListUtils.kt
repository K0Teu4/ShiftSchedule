package com.shiftschedule.app.util

object ListUtils {
    fun <T> move(list: List<T>, from: Int, to: Int): List<T> {
        if (from !in list.indices || to !in list.indices || from == to) return list
        val result = list.toMutableList()
        val item = result.removeAt(from)
        result.add(to, item)
        return result
    }
}