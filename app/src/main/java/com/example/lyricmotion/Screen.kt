package com.example.lyricmotion

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login   : Screen("login")
    object Register: Screen("register")
    object Home     : Screen("home")
    object Saved    : Screen("saved")
    object Settings : Screen("settings")
    object LyricsViewer : Screen("lyrics/{songId}") {
        const val ARG_SONG_ID = "songId"
        fun createRoute(songId: String) = "lyrics/$songId"
    }
}
