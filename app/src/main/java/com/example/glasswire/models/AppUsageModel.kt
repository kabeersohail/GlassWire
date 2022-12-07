package com.example.glasswire.models

data class AppUsageModel(
    val packageName: String,
    val sent: Long,
    val received: Long,
    val total: Long,
    val uid: Int,
    val systemApp: Boolean
)
