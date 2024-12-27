package net.minevn.libs

import java.time.ZonedDateTime

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CronTimer(val expression: String) {
    var nextSchedule = parseNextSchedule(); private set

    fun parseNextSchedule(time: ZonedDateTime) = getNextSchedule(expression, time)

    fun parseNextSchedule() = parseNextSchedule(ZonedDateTime.now())

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
