package com.example.glasswire.models

import android.graphics.drawable.Drawable

data class AppDataUsageModel(
    val applicationName: String,
    val packageName: String,
    val uid: Int,
    var isSystemApp: Boolean,
    var icon: Drawable
)