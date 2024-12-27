package net.minevn.libs

import java.time.ZonedDateTime

@Suppress("unused")
class CronTimer(val expression: String) {
    var nextSchedule = parseNextSchedule(); private set

    fun parseNextSchedule() = getNextSchedule(expression, ZonedDateTime.now())

    fun update() {
        nextSchedule = parseNextSchedule()
    }

    fun isTime(): Boolean {
        if (ZonedDateTime.now() >= nextSchedule) {
            update()
            return true
        }
        return false
    }
}
