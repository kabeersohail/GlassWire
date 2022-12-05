package com.example.glasswire

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.content.pm.PackageManager.GET_META_DATA
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Process.myUid
import android.os.RemoteException
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.glasswire.databinding.ActivityMainBinding
import com.example.glasswire.models.AppDataUsageModel
import com.example.glasswire.models.Duration
import com.example.glasswire.states.TimeFrame
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.time.*
import java.util.*

/**
 * Useful stack over flow links
 *
 * 1. https://stackoverflow.com/questions/36702621/getting-mobile-data-usage-history-using-networkstatsmanager (RnD)
 * 2. https://www.programcreek.com/java-api-examples/?api=android.app.usage.NetworkStatsManager (RnD)
 * 3.
 *
 */

/**
 * Article used to build this app
 *
 * 1. https://medium.com/@quiro91/build-a-data-usage-manager-in-android-e7991cfe7fe4
 */
open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Room for improvements
     *
     * 1. https://developer.android.com/training/permissions/requesting.html
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("TAG", "Granted")
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d("TAG", "Granted")
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var total: Float = 0F

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val selectedTimeFrame: TimeFrame = TimeFrame.Today

        binding.getWifiUsage.setOnClickListener {

            if(!checkForDataUsagePermission(this)) {
                handlePermission(it)
                return@setOnClickListener
            }

            /**
             * Room for improvements
             *
             * 1. https://developer.android.com/training/permissions/requesting.html
             */
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) -> {
                    CoroutineScope(Dispatchers.IO).launch {

                        val (start, end) = when(selectedTimeFrame) {
                            TimeFrame.LastMonth -> lastMonth()
                            TimeFrame.ThisMonth -> thisMonth()
                            TimeFrame.Today -> today()
                            TimeFrame.Yesterday -> yesterday()
                            TimeFrame.ThisYear -> thisYear()
                        }

                        getInstalledAppsCompat().forEach { app ->
                            /**
                             * Deprecation useful links
                             *
                             * TYPE_WIFI is deprecated so use NetworkCapabilities.TRANSPORT_WIFI
                             *
                             * 1. https://stackoverflow.com/questions/52816443/what-is-alternative-to-connectivitymanager-type-wifi-deprecated-in-android-p-api
                             * 2. https://stackoverflow.com/questions/56353916/connectivitymanager-type-wifi-is-showing-deprecated-in-code-i-had-use-network-ca
                             */
                            returnFormattedData(app.uid, start, end, NetworkCapabilities.TRANSPORT_WIFI).also { float ->
                                total += float
                            }
                        }

                        Log.d("SOHAIL TOTAL", "$total")
                        total = 0F
                    }
                }
                else -> requestPermissionLauncher.launch(READ_PHONE_STATE)
            }


        }

        binding.mobileDataUsageOfApps.setOnClickListener {

            if(!checkForDataUsagePermission(this)) {
                handlePermission(it)
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {

                val (start, end) = when(selectedTimeFrame) {
                    TimeFrame.LastMonth -> lastMonth()
                    TimeFrame.ThisMonth -> thisMonth()
                    TimeFrame.Today -> today()
                    TimeFrame.Yesterday -> yesterday()
                    TimeFrame.ThisYear -> thisYear()
                }

                getInstalledAppsCompat().forEach { app ->
                    returnFormattedData(app.uid, start, end, NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            }
        }

        binding.wifiUsage.setOnClickListener {
            val (sent, received, _) = getDeviceWifiDataUsageForToday()

            val (sentFormatted, receivedFormatted, totalFormatted) = formatData(sent, received)

            Log.d("SOHAIL BRO", "$sentFormatted, $receivedFormatted $totalFormatted")
        }
    }

    private fun today(): Duration = Duration(
        start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        end = ZonedDateTime.now().toInstant().toEpochMilli()
    )

    private fun yesterday(): Duration {
        val date = Date()
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, -1)
        c.time

        val startTime: Long = atStartOfDay(c.time) ?: throw Exception()
        val endTime: Long = atEndOfDay(c.time) ?: throw Exception()

        return Duration(startTime, endTime)
    }

    private fun thisMonth(): Duration {
        val c = Calendar.getInstance()
        c[Calendar.DAY_OF_MONTH] = 1
        val startTime: Long = atStartOfDay(c.time) ?: throw Exception()
        val endTime: Long = ZonedDateTime.now().toInstant().toEpochMilli()

        return Duration(startTime, endTime)
    }

    private fun lastMonth(): Duration {
        val aCalendar = Calendar.getInstance()
        aCalendar.add(Calendar.MONTH, -1)
        aCalendar[Calendar.DATE] = 1
        val firstDateOfPreviousMonth = aCalendar.time
        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lastDateOfPreviousMonth = aCalendar.time

        val startTime = atStartOfDay(firstDateOfPreviousMonth) ?: throw Exception()
        val endTime = atEndOfDay(lastDateOfPreviousMonth) ?: throw Exception()

        return Duration(startTime, endTime)
    }

    private fun thisYear(): Duration {
        val c = Calendar.getInstance()
        c[Calendar.DAY_OF_YEAR] = 1
        val startTime: Long = atStartOfDay(c.time) ?: throw Exception()
        val endTime: Long = ZonedDateTime.now().toInstant().toEpochMilli()
        return Duration(startTime, endTime)
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun returnFormattedData(uid: Int, startTime: Long, endTime: Long, type: Int): Float {
        val mData = if(type == NetworkCapabilities.TRANSPORT_WIFI) { getAppWifiDataUsage(uid, startTime, endTime) } else getAppMobileDataUsage(uid, startTime, endTime)
        val formattedData = formatData(mData[0], mData[1])
        Log.d("SOHAIL BRO", "${formattedData.toList()} $uid")
        return formattedData.last().split(" ").first().toFloat()
    }

    /**
     * Formats the data
     */
    private fun formatData(sent: Long, received: Long): Array<String> {
        val totalBytes = (sent + received) / 1024f
        val sentBytes = sent / 1024f
        val receivedBytes = received / 1024f

        val totalMB = totalBytes / 1024f

        val totalGB: Float
        val sentGB: Float
        val receivedGB: Float

        val sentMB: Float = sentBytes / 1024f
        val receivedMB: Float = receivedBytes / 1024f

        val sentData: String
        val receivedData: String
        val totalData: String
        if (totalMB > 1024) {
            totalGB = totalMB / 1024f
            totalData = String.format("%.2f", totalGB) + " GB"
        } else {
            totalData = String.format("%.2f", totalMB) + " MB"
        }

        if (sentMB > 1024) {
            sentGB = sentMB / 1024f
            sentData = String.format("%.2f", sentGB) + " GB"
        } else {
            sentData = String.format("%.2f", sentMB) + " MB"
        }
        if (receivedMB > 1024) {
            receivedGB = receivedMB / 1024f
            receivedData = String.format("%.2f", receivedGB) + " GB"
        } else {
            receivedData = String.format("%.2f", receivedMB) + " MB"
        }

        return arrayOf(sentData, receivedData, totalData)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(RemoteException::class, ParseException::class)
    private fun getAppWifiDataUsage(uid: Int, startTime: Long, endTime: Long): Array<Long> {

        var sent = 0L
        var received = 0L

        val networkStatsManager: NetworkStatsManager =
            applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
        val networkStats: NetworkStats = networkStatsManager.querySummary(
            NetworkCapabilities.TRANSPORT_WIFI,
            getSubscriberID(),
            startTime,
            endTime
        )

        do {
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)
            if (bucket.uid == uid) {
                sent += bucket.txBytes
                received += bucket.rxBytes
            }
        } while (networkStats.hasNextBucket())

        val total: Long = sent + received
        networkStats.close()

        return arrayOf(sent, received, total)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getDeviceWifiDataUsageForToday(): Array<Long> {

        val today = today()

        val networkStatsManager = getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
        val bucket: NetworkStats.Bucket = networkStatsManager.querySummaryForDevice(
            NetworkCapabilities.TRANSPORT_WIFI,
            getSubscriberID(),
            today.start,
            today.end
        )

        val received: Long = bucket.rxBytes
        val sent: Long = bucket.txBytes

        val total: Long = sent + received

        return arrayOf(sent, received, total)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(RemoteException::class, ParseException::class)
    private fun getAppMobileDataUsage(uid: Int, startTime: Long, endTime: Long): Array<Long> {

        var sent = 0L
        var received = 0L

        val networkStatsManager: NetworkStatsManager =
            applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
        val networkStats: NetworkStats = networkStatsManager.querySummary(
            NetworkCapabilities.TRANSPORT_CELLULAR,
            getSubscriberID(),
            startTime,
            endTime
        )

        do {
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)
            if (bucket.uid == uid) {
                sent += bucket.txBytes
                received += bucket.rxBytes
            }
        } while (networkStats.hasNextBucket())

        val total: Long = sent + received
        networkStats.close()

        return arrayOf(sent, received, total)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledAppsCompat(): List<AppDataUsageModel>  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(ApplicationInfoFlags.of(GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(GET_META_DATA)
        }.map { app ->
            AppDataUsageModel(packageManager.getApplicationLabel(app).toString(), app.packageName, app.uid, (app.flags and ApplicationInfo.FLAG_SYSTEM) == 1)
        }

    /**
     * Link describing the warning
     *
     * 1. https://stackoverflow.com/questions/47691310/why-is-using-getstring-to-get-device-identifiers-not-recommended
     */
    @SuppressLint("HardwareIds")
    private fun getSubscriberID(): String? = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) try {
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).subscriberId
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } else null


    /**
     * Displays a snack bar to request data usage permission
     */
    private fun handlePermission(view: View) {
        val snack = Snackbar.make(view,"App needs data usage access",Snackbar.LENGTH_LONG)
        snack.setAction("Grant") {
            startActivity(Intent(ACTION_USAGE_ACCESS_SETTINGS))
        }
        snack.show()
    }

    /**
     * Checks if data usage permission is granted
     *
     *Addressing deprecation
     *
     * 1. https://developer.android.com/reference/android/app/AppOpsManager.html#checkOpNoThrow(java.lang.String,%20int,%20java.lang.String)
     */
    private fun checkForDataUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), context.packageName)
        }
        return mode == MODE_ALLOWED
    }

}