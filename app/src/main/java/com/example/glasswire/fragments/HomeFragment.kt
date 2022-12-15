package com.example.glasswire.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.RemoteException
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.glasswire.R
import com.example.glasswire.databinding.FragmentHomeBinding
import com.example.glasswire.models.AppDataUsageModel
import com.example.glasswire.models.AppUsageModel
import com.example.glasswire.models.Duration
import com.example.glasswire.states.TimeFrame
import com.example.glasswire.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.time.*
import java.util.*

sealed interface DataFormat {
    object Decimal: DataFormat
    object Binary: DataFormat
}

class HomeFragment : Fragment() {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedTimeFrame: TimeFrame = TimeFrame.ThisYear

        fragmentHomeBinding.getWifiUsage.setOnClickListener { wifiUsageButton ->

            if(!checkForDataUsagePermission(requireContext())) {
                handlePermission(wifiUsageButton)
                return@setOnClickListener
            }

            /**
             * Room for improvements
             *
             * 1. https://developer.android.com/training/permissions/requesting.html
             */
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) -> {
                    CoroutineScope(Dispatchers.IO).launch {

                        val (start, end) = when(selectedTimeFrame) {
                            TimeFrame.LastMonth -> lastMonth()
                            TimeFrame.ThisMonth -> thisMonth()
                            TimeFrame.Today -> today()
                            TimeFrame.Yesterday -> yesterday()
                            TimeFrame.ThisYear -> thisYear()
                        }

                        val requiredList: List<AppUsageModel> = returnAppUsageModelList(start, end, NetworkCapabilities.TRANSPORT_WIFI)

                        sharedViewModel.setAppDataUsageList(requiredList)

                        withContext(Dispatchers.Main) {
                            wifiUsageButton.findNavController().navigate(R.id.action_homeFragment_to_recyclerViewFragment)
                        }
                    }
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }

        fragmentHomeBinding.mobileDataUsageOfApps.setOnClickListener { mobileDataUsageButton ->

            if(!checkForDataUsagePermission(requireContext())) {
                handlePermission(mobileDataUsageButton)
                return@setOnClickListener
            }

            /**
             * Room for improvements
             *
             * 1. https://developer.android.com/training/permissions/requesting.html
             */
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) -> {
                    CoroutineScope(Dispatchers.IO).launch {

                        val (start, end) = when(selectedTimeFrame) {
                            TimeFrame.LastMonth -> lastMonth()
                            TimeFrame.ThisMonth -> thisMonth()
                            TimeFrame.Today -> today()
                            TimeFrame.Yesterday -> yesterday()
                            TimeFrame.ThisYear -> thisYear()
                        }

                        val requiredList: List<AppUsageModel> = returnAppUsageModelList(start, end, NetworkCapabilities.TRANSPORT_CELLULAR)

                        sharedViewModel.setAppDataUsageList(requiredList)

                        requiredList.forEach {
                            Log.d("SOHAIL BRO", "$it")
                        }

                        withContext(Dispatchers.Main) {
                            mobileDataUsageButton.findNavController().navigate(R.id.action_homeFragment_to_recyclerViewFragment)
                        }
                    }
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }

        fragmentHomeBinding.wifiUsage.setOnClickListener {
            val (sent, received, total) = getDeviceWifiDataUsageForToday()

            Log.d("DataUsage-->", "$sent, $received $total")
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
    fun returnAppUsageModelList(start: Long, end: Long, networkType: Int): List<AppUsageModel> {
        val appUsageModelList: MutableList<AppUsageModel> = mutableListOf()

        getInstalledAppsCompat().forEach { app ->
            /**
             * Deprecation useful links
             *
             * TYPE_WIFI is deprecated so use NetworkCapabilities.TRANSPORT_WIFI
             *
             * 1. https://stackoverflow.com/questions/52816443/what-is-alternative-to-connectivitymanager-type-wifi-deprecated-in-android-p-api
             * 2. https://stackoverflow.com/questions/56353916/connectivitymanager-type-wifi-is-showing-deprecated-in-code-i-had-use-network-ca
             */

            returnFormattedData(
                app.uid,
                app.packageName,
                app.isSystemApp,
                start,
                end,
                networkType,
                app.icon
            )?.let {
                appUsageModelList.add(it)
            }
        }

        appUsageModelList.sortByDescending { it.total }

        return appUsageModelList
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun returnFormattedData(
        uid: Int,
        applicationName: String,
        isSystemApp: Boolean,
        startTime: Long,
        endTime: Long,
        type: Int,
        icon: Drawable
    ): AppUsageModel? {
        val (sent, received, total) = if (type == NetworkCapabilities.TRANSPORT_WIFI) {
            getAppWifiDataUsage(uid, startTime, endTime)
        } else getAppMobileDataUsage(uid, startTime, endTime)

        if(total <= 0) {
            return null
        }

        return AppUsageModel(applicationName, sent, icon, received, total, uid, isSystemApp)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(RemoteException::class, ParseException::class)
    private fun getAppWifiDataUsage(uid: Int, startTime: Long, endTime: Long): Array<Long> {

        var sent = 0L
        var received = 0L

        val networkStatsManager: NetworkStatsManager =
            requireContext().applicationContext.getSystemService(AppCompatActivity.NETWORK_STATS_SERVICE) as NetworkStatsManager
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

        val networkStatsManager = requireContext().applicationContext.getSystemService(AppCompatActivity.NETWORK_STATS_SERVICE) as NetworkStatsManager
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
            requireContext().applicationContext.getSystemService(AppCompatActivity.NETWORK_STATS_SERVICE) as NetworkStatsManager
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
        requireContext().packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(
                PackageManager.GET_META_DATA.toLong()))
    } else {
        @Suppress("DEPRECATION")
        requireContext().packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }.map { app ->
        AppDataUsageModel(requireContext().packageManager.getApplicationLabel(app).toString(), app.packageName, app.uid, (app.flags and ApplicationInfo.FLAG_SYSTEM) == 1, requireContext().packageManager.getApplicationIcon(app.packageName))
    }

    /**
     * Link describing the warning
     *
     * 1. https://stackoverflow.com/questions/47691310/why-is-using-getstring-to-get-device-identifiers-not-recommended
     */
    @SuppressLint("HardwareIds")
    private fun getSubscriberID(): String? = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) try {
        (requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager).subscriberId
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } else null


    /**
     * Displays a snack bar to request data usage permission
     */
    private fun handlePermission(view: View) {
        val snack = Snackbar.make(view,"App needs data usage access", Snackbar.LENGTH_LONG)
        snack.setAction("Grant") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

}