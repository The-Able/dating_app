package com.libra.tools

import com.libra.Constants
import java.util.Calendar
import java.util.Date


object LoginTimesHelper {
    val loginTimes: List<Calendar>
        get() = Constants.openHoursList.map(::createCalendarFromHour)

    fun findNextLoginHourForNotificationFromNow(): Calendar {
        val now = Date()
        val loginWindows = loginTimes.map { it.apply { it.add(Calendar.MINUTE, -20) } }
        while (true) {
            val next = loginWindows.firstOrNull { it.time.after(now) }
            if (next == null) {
                loginWindows.forEach {
                    it.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else {
                return next
            }
        }
    }

    fun findNextLoginHourForFromNow(): Calendar {
        val now = Date()
        val loginWindows = loginTimes.map { it.apply { it } }
        while (true) {
            val next = loginWindows.firstOrNull { it.time.after(now) }
            if (next == null) {
                loginWindows.forEach {
                    it.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else {
                return next
            }
        }
    }

    private fun createCalendarFromHour(hour: Int): Calendar = Calendar.getInstance().apply {
        clear(Calendar.MINUTE)
        clear(Calendar.SECOND)
        clear(Calendar.MILLISECOND)
        set(Calendar.HOUR_OF_DAY, hour)
    }
}