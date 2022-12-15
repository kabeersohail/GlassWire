package com.example.glasswire

import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

class DatesTest {

    @Test
    fun `first test`() {

        val knowledge = Knowledge()

        val lastThirtiethDay: Date = GregorianCalendar().apply {
            time = Date()
            add(Calendar.DAY_OF_MONTH, -30)
        }.time

        val startOfLastThirtiethDay: Long = knowledge.atStartOfDay(lastThirtiethDay).toInstant().toEpochMilli()
        val now: Long = ZonedDateTime.now().toInstant().toEpochMilli()

        Assert.assertEquals(1667845800000, startOfLastThirtiethDay)
    }
}

@Suppress("unused")
class Knowledge {

    fun getLast30Days(): Date {
        val today = Date()
        val cal: Calendar = GregorianCalendar()
        cal.time = today
        cal.add(Calendar.DAY_OF_MONTH, -30)
        return cal.time
    }

    fun getLast60Days(): Date {
        val today = Date()
        val cal: Calendar = GregorianCalendar()
        cal.time = today
        cal.add(Calendar.DAY_OF_MONTH, -60)
        return cal.time
    }

    fun getLast90Days(): Date {
        val today = Date()
        val cal: Calendar = GregorianCalendar()
        cal.time = today
        cal.add(Calendar.DAY_OF_MONTH, -90)
        return cal.time
    }

    fun atStartOfDay(date: Date): Date {
        val localDateTime: LocalDateTime = dateToLocalDateTime(date)
        val startOfDay: LocalDateTime = localDateTime.with(LocalTime.MIN)
        return localDateTimeToDate(startOfDay)
    }

    fun atEndOfDay(date: Date): Date {
        val localDateTime: LocalDateTime = dateToLocalDateTime(date)
        val endOfDay: LocalDateTime = localDateTime.with(LocalTime.MAX)
        return localDateTimeToDate(endOfDay)
    }

    private fun dateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    private fun localDateTimeToDate(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

    fun main() {
        val dateFormat = SimpleDateFormat("dd-MMM-yy hh.mm.ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())
        println(formattedDate)

        val date = SimpleDateFormat("dd-MMM-yy hh.mm.ss").parse(formattedDate)
        println(date.time)
    }

}