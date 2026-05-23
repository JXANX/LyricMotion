package com.lyricmotion.data

data class AppSettings(
    val defaultStyleIndex: Int     = 0,
    val fontSize:          Float   = 16f,
    val animationSpeed:    Float   = 1f,
    val autoPlay:          Boolean = true
)
