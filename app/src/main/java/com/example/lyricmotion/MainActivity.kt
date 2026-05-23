package com.lyricmotion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lyricmotion.data.AppSettings
import com.lyricmotion.data.sampleSongs
import com.lyricmotion.ui.screens.HomeScreen
import com.lyricmotion.ui.screens.LoginScreen
import com.lyricmotion.ui.screens.LyricsViewerScreen
import com.lyricmotion.ui.screens.RegisterScreen
import com.lyricmotion.ui.screens.SavedScreen
import com.lyricmotion.ui.screens.SettingsScreen
import com.lyricmotion.ui.screens.WelcomeScreen
import com.lyricmotion.ui.theme.LyricMotionTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// ================================================================
//  MAIN ACTIVITY
// ================================================================

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var pendingSongId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        pendingSongId = intent?.getStringExtra(LyricsService.EXTRA_SONG_ID)
        ReminderWorker.schedule(this)

        setContent {
            LyricMotionTheme {
                LyricMotionApp(initialSongId = pendingSongId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingSongId = intent.getStringExtra(LyricsService.EXTRA_SONG_ID)
        setContent {
            LyricMotionTheme {
                LyricMotionApp(initialSongId = pendingSongId)
            }
        }
    }
}

// ================================================================
//  APP ROOT
// ================================================================

@Composable
fun LyricMotionApp(initialSongId: String? = null) {
    val navController   = rememberNavController()
    val context         = LocalContext.current
    val userManager     = remember { UserManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    val loggedEmail     by userManager.loggedUserEmail.collectAsState(initial = "")
    val isLoggedIn      by userManager.isLoggedIn.collectAsState(initial = false)

    val settings by remember(loggedEmail) {
        if (loggedEmail.isNotEmpty()) settingsManager.getSettingsForUser(loggedEmail)
        else flowOf(AppSettings())
    }.collectAsState(initial = AppSettings())

    val savedSongIds by remember(loggedEmail) {
        if (loggedEmail.isNotEmpty()) userManager.getSavedSongIds(loggedEmail)
        else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    LaunchedEffect(initialSongId, isLoggedIn) {
        if (!initialSongId.isNullOrEmpty() && isLoggedIn) {
            navController.navigate(Screen.LyricsViewer.createRoute(initialSongId)) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }
            WelcomeScreen(
                onComenzarClick = { navController.navigate(Screen.Login.route) },
                onYaTengoClick  = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, userManager = userManager)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, userManager = userManager)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Saved.route) {
            val savedSongs = sampleSongs.filter { savedSongIds.contains(it.id) }
            SavedScreen(
                navController = navController,
                savedSongs    = savedSongs,
                onRemove      = { songId ->
                    scope.launch { userManager.removeSongForUser(loggedEmail, songId) }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                userManager   = userManager,
                userEmail     = loggedEmail
            )
        }
        composable(
            route     = Screen.LyricsViewer.route,
            arguments = listOf(navArgument(Screen.LyricsViewer.ARG_SONG_ID) { type = NavType.StringType })
        ) { backStack ->
            val songId  = backStack.arguments?.getString(Screen.LyricsViewer.ARG_SONG_ID) ?: ""
            val song    = sampleSongs.find { it.id == songId } ?: sampleSongs[0]
            val isSaved = savedSongIds.contains(song.id)

            DisposableEffect(song) {
                LyricsService.startService(context, song.id, song.title, song.artist, song.lyrics)
                onDispose { LyricsService.stopService(context) }
            }

            LyricsViewerScreen(
                song         = song,
                settings     = settings,
                isSaved      = isSaved,
                onToggleSave = {
                    scope.launch {
                        if (isSaved) userManager.removeSongForUser(loggedEmail, song.id)
                        else         userManager.saveSongForUser(loggedEmail, song.id)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
