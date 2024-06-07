package com.grid.pos.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateHelper {

    fun getDateInFormat(
            date: Date = Date(),
            format: String = "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
    ): String {
        val parserFormat = SimpleDateFormat(
            format,
            Locale.getDefault()
        )
        parserFormat.timeZone = TimeZone.getTimeZone("UTC")
        return parserFormat.format(date)
    }

    fun getDateFromString(
            date: String,
            format: String
    ): Date {
        return SimpleDateFormat(
            format,
            Locale.getDefault()
        ).parse(date)!!
    }

    fun editDate(
            date: Date = Date(),
            hours: Int = 23,
            minutes: Int = 59,
            seconds: Int = 59
    ): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date

        calendar.set(
            Calendar.HOUR_OF_DAY,
            hours
        )
        calendar.set(
            Calendar.MINUTE,
            minutes
        )
        calendar.set(
            Calendar.SECOND,
            seconds
        )
        return calendar.time
    }

    fun getDatesDiff(
            date1: Date,
            date2: Date
    ): Long {
        val diff = date1.time - date2.time
        return diff / (24 * 60 * 60 * 1000)
    }
}