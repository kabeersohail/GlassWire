package com.example.glasswire.models

import android.graphics.drawable.Drawable

data class AppUsageModel(
    val packageName: String,
    val applicationName: String,
    val sent: Long,
    val icon: Drawable,
    val received: Long,
    val total: Long,
    val uid: Int,
    val systemApp: Boolean
)
