package com.example.glasswire

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process.myUid
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import android.telephony.TelephonyManager
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.glasswire.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Article used to build this app
 *
 * 1. https://medium.com/@quiro91/build-a-data-usage-manager-in-android-e7991cfe7fe4
 */
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.actualAction.setOnClickListener {

            if(!checkForPermission(this)) {
                handlePermission(it)
                return@setOnClickListener
            }

            val networkStatsManager = this.getSystemService(NETWORK_STATS_SERVICE)

        }

    }


    /**
     * Link to address deprecation
     *
     * 1. https://stackoverflow.com/questions/66896154/getinstallerpackagenamestring-string-is-deprecated-deprecated-in-java
     */
    private fun uidOf(mPackage: String) = packageManager.getApplicationInfo("com.example.app", 0).uid

    /**
     * Link describing the warning
     *
     * 1. https://stackoverflow.com/questions/47691310/why-is-using-getstring-to-get-device-identifiers-not-recommended
     */
    private fun getSubscriberID(): String = (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).subscriberId

    /**
     * Displays a snack bar to request data usage permission
     */
    private fun handlePermission(view: View) {
        val snack = Snackbar.make(view,"App needs data usage access",Snackbar.LENGTH_LONG)
        snack.setAction("Grant") {
            startActivity(Intent(ACTION_USAGE_ACCESS_SETTINGS));
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