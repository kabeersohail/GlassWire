package com.example.glasswire.models

import android.graphics.drawable.Drawable

data class AppUsageModel(
    val packageName: String,
    val sent: String,
    val icon: Drawable,
    val received: String,
    val total: String,
    val uid: Int,
    val systemApp: Boolean
)
