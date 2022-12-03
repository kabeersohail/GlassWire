package com.example.glasswire

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `start time of day first method`() {
        val localDate: LocalDate = LocalDate.parse(getTodayDate())

        val startOfDay: LocalDateTime = localDate.atStartOfDay()
        assertEquals("2022-12-03T00:00", startOfDay.toString())
    }

    @Test
    fun `start time of day second method`() {
        val localDate: LocalDate = LocalDate.parse(getTodayDate())
        val startOfDay = LocalDateTime.of(localDate, LocalTime.MIDNIGHT)

        assertEquals("2022-12-03T00:00", startOfDay.toString())
    }

    @Test
    fun `end time of day first method`() {
        val localDate: LocalDate = LocalDate.parse(getTodayDate())
        val endOfDay = LocalDateTime.of(localDate, LocalTime.MAX)

        assertEquals("2022-12-03T23:59:59.999999999", endOfDay.toString())
    }

    @Test
    fun `end time of day second method`() {
        val localDate: LocalDate = LocalDate.parse(getTodayDate())
        val endOfDay = localDate.atTime(LocalTime.MAX)

        assertEquals("2022-12-03T23:59:59.999999999", endOfDay.toString())
    }

    @Test
    fun `get today dats in Long`() {
        val date: Date = Date()
        val startTime = atStartOfDay(date)
        val endTime = atEndOfDay(date)

        assertEquals(1670005800000, startTime)
        assertEquals(1670092199999, endTime)
    }

    @Test
    fun `get yesterday dats in Long`() {
        val date = Date()
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, -1)
        c.time

        val startTime = atStartOfDay(c.time)
        val endTime = atEndOfDay(c.time)

        assertEquals(1669919400000, startTime)
        assertEquals(1670005799999, endTime)
    }

    fun getYesterdayDate() {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        dateFormat.format(cal.time) //your formatted date here
    }

    private fun getTodayDate(): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DATE, 0)
        return dateFormat.format(cal.time) //your formatted date here
    }



    fun atStartOfDay(date: Date): Long? {
        val localDateTime = dateToLocalDateTime(date)
        val startOfDay = localDateTime.with(LocalTime.MIN)
        return localDateTimeToDate(startOfDay)?.toInstant()?.toEpochMilli()
    }

    fun atEndOfDay(date: Date): Long? {
        val localDateTime = dateToLocalDateTime(date)
        val endOfDay = localDateTime.with(LocalTime.MAX)
        return localDateTimeToDate(endOfDay)?.toInstant()?.toEpochMilli()
    }

    private fun dateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    private fun localDateTimeToDate(localDateTime: LocalDateTime): Date? {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

}

