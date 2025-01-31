package com.grid.pos.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateHelper {

    fun getDateInFormat(
        date: Date = Date(),
        format: String = "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
    ): String {
        val parserFormat = SimpleDateFormat(
            format,
            Locale.getDefault()
        )
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
        calendar.set(
            Calendar.MILLISECOND,
            0
        )
        return calendar.time
    }

    fun addDays(
        initialDate: Date = Date(),
        days: Int
    ): Date {
        val cal = Calendar.getInstance()
        cal.time = initialDate
        cal.add(
            Calendar.DATE,
            days
        )

        val date = initialDate.clone() as Date
        date.time = cal.timeInMillis

        return date
    }

    fun getDaysDiff(
        date1: Date,
        date2: Date
    ): Long {
        // Calculate the difference in milliseconds
        val differenceInMillis = date2.time - date1.time
        // Convert milliseconds to days
        return TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    }
}