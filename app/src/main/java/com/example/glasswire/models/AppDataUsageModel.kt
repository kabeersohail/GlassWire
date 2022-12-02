package com.example.glasswire.models

data class AppDataUsageModel(
    val applicationName: String,
    val packageName: String,
    val uid: Int,
    var isSystemApp: Boolean
)