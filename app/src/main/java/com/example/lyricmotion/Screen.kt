package com.lyricmotion

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login   : Screen("login")
    data object Register: Screen("register")
    data object Home     : Screen("home")
    data object Saved    : Screen("saved")
    data object Settings : Screen("settings")
    data object LyricsViewer : Screen("lyrics/{songId}") {
        const val ARG_SONG_ID = "songId"
        fun createRoute(songId: String) = "lyrics/$songId"
    }
}
