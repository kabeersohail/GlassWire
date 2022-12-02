package com.example.glasswire

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Process.myUid
import android.os.RemoteException
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.glasswire.databinding.ActivityMainBinding
import com.example.glasswire.models.AppDataUsageModel
import com.example.glasswire.models.Duration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Article used to build this app
 *
 * 1. https://medium.com/@quiro91/build-a-data-usage-manager-in-android-e7991cfe7fe4
 */
open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.actualAction.setOnClickListener {

            if(!checkForPermission(this)) {
                handlePermission(it)
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {

                val (start, end) = today()

                getAllInstalledAppsData().forEach { app ->
                    returnFormattedData(app.uid, start, end)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun today(): Duration = Duration(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), ZonedDateTime.now().toInstant().toEpochMilli())

    @RequiresApi(Build.VERSION_CODES.M)
    fun returnFormattedData(uid: Int, startTime: Long, endTime: Long) {
        val mData = getAppWifiDataUsage(uid, startTime, endTime)
        val formattedData = formatData(mData[0], mData[1])
        Log.d("SOHAIL BRO", "${formattedData.toList()} $uid")
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

    @Throws(RemoteException::class, ParseException::class)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAppWifiDataUsage(uid: Int, startTime: Long, endTime: Long): Array<Long> {

        var sent = 0L
        var received = 0L

        val networkStatsManager: NetworkStatsManager =
            applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
        val networkStats: NetworkStats = networkStatsManager.querySummary(
            ConnectivityManager.TYPE_WIFI,
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

    private fun getAllInstalledAppsData(): List<AppDataUsageModel> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map { app ->
        AppDataUsageModel(packageManager.getApplicationLabel(app).toString(), app.packageName, app.uid, (app.flags and ApplicationInfo.FLAG_SYSTEM) == 1)
    }

    /**
     * Link describing the warning
     *
     * 1. https://stackoverflow.com/questions/47691310/why-is-using-getstring-to-get-device-identifiers-not-recommended
     */
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
     */
    private fun checkForPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), context.packageName)
        return mode == MODE_ALLOWED
    }

}