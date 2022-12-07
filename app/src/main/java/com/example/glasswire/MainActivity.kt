package com.example.glasswire

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.glasswire.databinding.ActivityMainBinding
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

}