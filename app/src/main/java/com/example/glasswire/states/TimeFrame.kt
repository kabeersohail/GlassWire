package com.example.glasswire.states

sealed class TimeFrame {
    object Today: TimeFrame()
    object Yesterday: TimeFrame()
    object ThisMonth: TimeFrame()
    object LastMonth: TimeFrame()
    object ThisYear: TimeFrame()
}