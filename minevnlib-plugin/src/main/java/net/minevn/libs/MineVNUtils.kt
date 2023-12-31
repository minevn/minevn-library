package net.minevn.libs

import java.util.*

private fun getCalendar() = Calendar.getInstance(Locale.forLanguageTag("en-150"))!!.apply {
    firstDayOfWeek = Calendar.MONDAY
}

/**
 * = true nếu tuần hiện tại nằm ở năm trước nhiều ngày hơn
 */
fun weekBelongsToLastYear(time: Date) = getCalendar().run {
    this.time = time
    // lui về thứ 2 của tuần giữa 2 năm
    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // nếu thứ 2 của tuần đầu năm rơi vào ngày 28/12 trở về trước của năm trước, thì tuần đầu năm
    // chiếm >= 4 ngày của năm trước và sẽ tính vào năm trước đó
    get(Calendar.MONTH) == 11 && get(Calendar.DAY_OF_MONTH) <= 28
}

/**
 * = true nếu tuần đầu tiên của năm nay nằm ở năm trước nhiều hơn
 */
fun firstWeekBelongsToLastYear(time: Date) = getCalendar().run {
    this.time = time
    // lui về ngày 1/1 của năm hiện tại
    set(Calendar.MONTH, 0)
    set(Calendar.DATE, 1)
    weekBelongsToLastYear(this.time)
}

fun getWeekOfYear(time: Date) = getCalendar().run {
    this.time = time
    var week = get(Calendar.WEEK_OF_YEAR)
    if (week == 1) {
        // tuần giữa 2 năm
        week = if (weekBelongsToLastYear(time)) {
            // tuần thuộc về năm trước
            // lui về 1 tuần rồi tính theo năm trước
            add(Calendar.DAY_OF_MONTH, -7)
            get(Calendar.WEEK_OF_YEAR)
        } else {
            // tuần thuộc về năm sau
            // tiến tới 1 tuần rồi tính theo năm sau
            add(Calendar.DAY_OF_MONTH, + 7)
            get(Calendar.WEEK_OF_YEAR) - 1
        }
    } else if (firstWeekBelongsToLastYear(time)) week--
    YearWeek(get(Calendar.YEAR), week)
}

class YearWeek(var year: Int, var week: Int) {
    override fun toString() = "${year}_w${week.toString().padStart(2, '0')}"
}

val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("en-150"))

fun Long.timeToString() = dateFormat.format(Date(this))!!