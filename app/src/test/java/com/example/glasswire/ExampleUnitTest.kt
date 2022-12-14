package com.example.glasswire

import android.os.Build
import androidx.annotation.RequiresApi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

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

    @Test
    fun `last month start date and end date`() {
        val aCalendar = Calendar.getInstance()

        aCalendar.add(Calendar.MONTH, -1)

        aCalendar[Calendar.DATE] = 1

        val firstDateOfPreviousMonth = aCalendar.time

        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val lastDateOfPreviousMonth = aCalendar.time

        val startTime = atStartOfDay(firstDateOfPreviousMonth)
        val endTime = atEndOfDay(lastDateOfPreviousMonth)

        assertEquals(1667241000000, startTime)
        assertEquals(1669832999999, endTime)
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



    private fun atStartOfDay(date: Date): Long? {
        val localDateTime = dateToLocalDateTime(date)
        val startOfDay = localDateTime.with(LocalTime.MIN)
        return localDateTimeToDate(startOfDay)?.toInstant()?.toEpochMilli()
    }

    private fun atEndOfDay(date: Date): Long? {
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


    /**
     *
     *
     *
     *
     *
     *
     *
     */


    @RequiresApi(Build.VERSION_CODES.O)
    fun getPrev30DaysTime() : Long {
        val today = Date()
        val cal: Calendar = GregorianCalendar()
        cal.time = today
        cal.add(Calendar.DAY_OF_MONTH, -30)
        val today30: Date = cal.time
        return atStartOfDayX(today30)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun atStartOfDayX(date: Date): Long {
        val localDateTime = dateToLocalDateTimeX(date)
        val startOfDay = localDateTime.with(LocalTime.MIN)
        return localDateTimeToDateX(startOfDay).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun localDateTimeToDateX(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateToLocalDateTimeX(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    @Test
    fun testFormatData() {
        val actualResult = formatData(557685071)
        assertEquals(1, actualResult)
    }

    @Test
    fun testFormatDataNew() {
        val actualResult = formatDataNew(557685071, "binary")
        assertEquals(1, actualResult)
    }

    private fun formatDataNew(bytes: Long, decimalOrBinary: String): String {
        if (bytes == 0L) return "0 Bytes"

        var k: Long = 0L
        var i: Long = 0L

        val dm = 2

        val sizes: List<String> = listOf("Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")

        k = if (decimalOrBinary == "binary") 1024 else 1000

        i = (floor(ln(bytes.toDouble())) / ln(k.toDouble())).toLong()

        return "${(bytes / k.toDouble().pow(i.toDouble()))} + ${sizes[i.toInt()]}"

    }

    private fun formatData(total: Long): String {
        val totalBytes = total / 1024f

        val totalMB = totalBytes / 1024f

        val totalGB: Float

        val totalData: String
        if (totalMB > 1024) {
            totalGB = totalMB / 1024f
            totalData = String.format("%.2f", totalGB) + " GB"
        } else {
            totalData = String.format("%.2f", totalMB) + " MB"
        }

        return totalData
    }

}

