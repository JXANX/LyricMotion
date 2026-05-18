package com.lyricmotion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyricmotion.ui.theme.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

// ================================================================
//  MAIN ACTIVITY
// ================================================================

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        ReminderWorker.schedule(this)

        setContent {
            LyricMotionTheme {
                LyricMotionApp()
            }
        }
    }
}

// ================================================================
//  APP ROOT
// ================================================================

@Composable
fun LyricMotionApp() {
    val navController   = rememberNavController()
    val context         = LocalContext.current
    val userManager     = remember { UserManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    val loggedEmail     by userManager.loggedUserEmail.collectAsState(initial = "")

    // Ajustes por usuario
    val settings by remember(loggedEmail) {
        if (loggedEmail.isNotEmpty()) settingsManager.getSettingsForUser(loggedEmail)
        else flowOf(AppSettings())
    }.collectAsState(initial = AppSettings())

    // Canciones guardadas por usuario
    val savedSongIds by remember(loggedEmail) {
        if (loggedEmail.isNotEmpty()) userManager.getSavedSongIds(loggedEmail)
        else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    NavHost(
        navController    = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
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
                LyricsService.startService(context, song.title, song.artist, song.lyrics)
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

// ================================================================
//  DATOS — letras completas
// ================================================================

enum class SearchState { IDLE, LOADING, EMPTY, ERROR, RESULTS }
enum class LyricStyle  { NEON, KARAOKE, FADE }

data class SongItem(
    val id:       String,
    val title:    String,
    val artist:   String,
    val duration: String,
    val lyrics:   String = ""
)

val sampleSongs = listOf(
    SongItem("1", "Bohemian Rhapsody", "Queen", "5:55",
        "Is this the real life?\nIs this just fantasy?\nCaught in a landslide\nNo escape from reality\n\nOpen your eyes\nLook up to the skies and see\nI'm just a poor boy, I need no sympathy\nBecause it's easy come, easy go\nLittle high, little low\nAnyway the wind blows doesn't really matter to me, to me\n\nMama, just killed a man\nPut a gun against his head\nPulled my trigger, now he's dead\nMama, life had just begun\nBut now I've gone and thrown it all away\n\nMama, ooh\nDidn't mean to make you cry\nIf I'm not back again this time tomorrow\nCarry on, carry on as if nothing really matters\n\nToo late, my time has come\nSends shivers down my spine\nBody's aching all the time\nGoodbye, everybody, I've got to go\nGotta leave you all behind and face the truth\n\nMama, ooh\nI don't want to die\nI sometimes wish I'd never been born at all\n\nI see a little silhouetto of a man\nScaramouche, Scaramouche, will you do the Fandango?\nThunderbolt and lightning very, very frightening me\nGalileo, Galileo\nGalileo figaro, magnifico\n\nI'm just a poor boy, nobody loves me\nHe's just a poor boy from a poor family\nSpare him his life from this monstrosity\n\nEasy come, easy go, will you let me go?\nBismillah! No, we will not let you go\nLet him go!"),

    SongItem("2", "Imagine", "John Lennon", "3:07",
        "Imagine there's no heaven\nIt's easy if you try\nNo hell below us\nAbove us, only sky\nImagine all the people living for today\n\nImagine there's no countries\nIt isn't hard to do\nNothing to kill or die for\nAnd no religion, too\nImagine all the people living life in peace\n\nYou may say I'm a dreamer\nBut I'm not the only one\nI hope someday you'll join us\nAnd the world will be as one\n\nImagine no possessions\nI wonder if you can\nNo need for greed or hunger\nA brotherhood of man\nImagine all the people sharing all the world\n\nYou may say I'm a dreamer\nBut I'm not the only one\nI hope someday you'll join us\nAnd the world will live as one"),

    SongItem("3", "Starboy", "The Weeknd", "3:50",
        "I'm tryna put you in the worst mood, ah\nP1 cleaner than your church shoes, ah\nMilli point two just to hurt you, ah\nAll red Lamb' just to swerve you, ah\n\nUsed to call me on my cell phone\nLate night when you need my love\nCall me on my cell phone\nLate night when you need my love\n\nLook what you've done\nI'm a starboy\nLook what you've done\nI'm a starboy\n\nEvery day a nigga try to test me\nEvery day a nigga try to test me\nAll my nightmares, they a blessing\nAll my nightmares, they a blessing\n\nHouse so empty, need a centerpiece\nTwenty racks a table cut from ebony\nCut that ivory into skinny pieces\nThen she clean it with her face, man I love my baby"),

    SongItem("4", "Blinding Lights", "The Weeknd", "3:20",
        "I've been on my own for long enough\nMaybe you can show me how to love, maybe\nI'm going through withdrawals\n\nYou don't even have to do too much\nYou can turn me on with just a touch, baby\n\nI look around and sin city's cold and empty\nNo one's around to judge me\nI can't see clearly when you're gone\n\nI said, ooh, I'm blinded by the lights\nNo, I can't sleep until I feel your touch\nI said, ooh, I'm drowning in the night\nOh, when I'm like this, you're the one I trust\n\nI'm running out of time\nCause I can see the sun light up the sky\nSo I hit the road in overdrive, baby\n\nThe city's cold and empty\nNo one's around to judge me\nI can't see clearly when you're gone\n\nI said, ooh, I'm blinded by the lights\nNo, I can't sleep until I feel your touch"),

    SongItem("5", "Shape of You", "Ed Sheeran", "3:53",
        "The club isn't the best place to find a lover\nSo the bar is where I go\nMe and my friends at the table doing shots\nDrinking fast and then we talk slow\n\nCome over and start up a conversation with just me\nAnd trust me I'll give it a chance now\nTake my hand, stop, put Van the Man on the jukebox\nAnd then we start to dance\n\nGirl, you know I want your love\nYour love was handmade for somebody like me\nCome on now, follow my lead\nI may be crazy, don't mind me\n\nI'm in love with the shape of you\nWe push and pull like a magnet do\nAlthough my heart is falling too\nI'm in love with your body\n\nLast night you were in my room\nAnd now my bedsheets smell like you\nEvery day discovering something brand new\nI'm in love with your body"),

    SongItem("6", "Perfect", "Ed Sheeran", "4:23",
        "I found a love for me\nDarling, just dive right in and follow my lead\nWell, I found a girl, beautiful and sweet\nOh, I never knew you were the someone waiting for me\n\nCause we were just kids when we fell in love\nNot knowing what it was\nI will not give you up this time\n\nDarling, just kiss me slow, your heart is all I own\nAnd in your eyes you're holding mine\n\nBaby, I'm dancing in the dark\nWith you between my arms\nBarefoot on the grass\nListening to our favourite song\n\nWhen you said you looked a mess\nI whispered underneath my breath\nBut you heard it, darling, you look perfect tonight\n\nWell I found a woman, stronger than anyone I know\nShe shares my dreams, I hope that someday I'll share her home\nI found a love, to carry more than just my secrets\nTo carry love, to carry children of our own"),

    SongItem("7", "Hallelujah", "Leonard Cohen", "4:36",
        "I've heard there was a secret chord\nThat David played, and it pleased the Lord\nBut you don't really care for music, do you?\nIt goes like this: the fourth, the fifth\nThe minor fall, the major lift\nThe baffled king composing Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah\n\nYour faith was strong but you needed proof\nYou saw her bathing on the roof\nHer beauty in the moonlight overthrew you\nShe tied you to a kitchen chair\nShe broke your throne, and she cut your hair\nAnd from your lips she drew the Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah\n\nMaybe there's a God above\nBut all I've ever learned from love\nWas how to shoot at someone who outdrew you\nIt's not a cry you can hear at night\nIt's not somebody who has seen the light\nIt's a cold and it's a broken Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah"),

    SongItem("8", "Yellow", "Coldplay", "4:29",
        "Look at the stars\nLook how they shine for you\nAnd everything you do\nYeah, they were all yellow\n\nI came along\nI wrote a song for you\nAnd all the things you do\nAnd it was called Yellow\n\nSo then I took my turn\nOh, what a thing to have done\nAnd it was all yellow\n\nYour skin, oh yeah, your skin and bones\nTurn into something beautiful\nDo you know, you know I love you so\nYou know I love you so\n\nI swam across, I jumped across for you\nOh, what a thing to do\nCause you were all yellow\n\nI drew a line, I drew a line for you\nOh, what a thing to do\nAnd it was all yellow\n\nYour skin, oh yeah your skin and bones\nTurn into something beautiful\nAnd you know, for you I'd bleed myself dry\nFor you I'd bleed myself dry\n\nIt's true, look how they shine for you\nLook how they shine for you\nLook how they shine\nLook at the stars, look how they shine for you\nAnd all the things that you do"),

    SongItem("9", "Someone Like You", "Adele", "4:45",
        "I heard that you're settled down\nThat you found a girl and you're married now\nI heard that your dreams came true\nGuess she gave you things I didn't give to you\n\nOld friend, why are you so shy?\nAin't like you to hold back or hide from the light\n\nI hate to turn up out of the blue uninvited\nBut I couldn't stay away, I couldn't fight it\nI had hoped you'd see my face\nAnd that you'd be reminded that for me it isn't over\n\nNever mind, I'll find someone like you\nI wish nothing but the best for you, too\nDon't forget me, I beg, I remember you said\nSometimes it lasts in love, but sometimes it hurts instead\n\nYou know how the time flies\nOnly yesterday was the time of our lives\nWe were born and raised in a summer haze\nBound by the surprise of our glory days\n\nNever mind, I'll find someone like you\nI wish nothing but the best for you, too\nDon't forget me, I beg, I remember you said\nSometimes it lasts in love, but sometimes it hurts instead"),

    SongItem("10", "Rolling in the Deep", "Adele", "3:48",
        "There's a fire starting in my heart\nReaching a fever pitch and it's bringing me out the dark\nFinally, I can see you crystal clear\nGo ahead and sell me out and I'll lay your ship bare\n\nSee how I'll leave with every piece of you\nDon't underestimate the things that I will do\n\nThe scars of your love remind me of us\nThey keep me thinking that we almost had it all\nThe scars of your love, they leave me breathless\nI can't help feeling\n\nWe could have had it all\nRolling in the deep\nYou had my heart inside of your hand\nAnd you played it to the beat\n\nBaby, I have no story to be told\nBut I've heard one on you and I'm gonna make your head burn\nThink of me in the depths of your despair\nMaking a home down there as mine sure won't be shared\n\nWe could have had it all\nRolling in the deep\nYou had my heart inside of your hand\nAnd you played it to the beat\n\nThrow your soul through every open door\nCount your blessings to find what you look for\nTurn my sorrow into treasured gold\nYou pay me back in kind and reap just what you sow")
)

val featuredSongs = sampleSongs.take(3)

// ================================================================
//  FUENTES POR ESTILO
// ================================================================

val fontNeon    = FontFamily.Monospace
val fontKaraoke = FontFamily.Serif
val fontFade    = FontFamily.SansSerif

fun lyricFontFamily(style: LyricStyle) = when (style) {
    LyricStyle.NEON    -> fontNeon
    LyricStyle.KARAOKE -> fontKaraoke
    LyricStyle.FADE    -> fontFade
}

// ================================================================
//  COMPONENTES COMPARTIDOS
// ================================================================

@Composable
fun AppLogo(size: Int = 72) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(LMPrimaryVariant, LMPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size((size * 0.44).dp))
    }
}

@Composable
fun LMBottomNav(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(containerColor = LMSurface, tonalElevation = 0.dp) {
        listOf(
            Triple(Icons.Default.Home,           "Inicio",    Screen.Home.route),
            Triple(Icons.Default.BookmarkBorder, "Guardadas", Screen.Saved.route),
            Triple(Icons.Default.Settings,       "Ajustes",   Screen.Settings.route),
        ).forEach { (icon, label, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick  = {
                    if (currentRoute != route) navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon   = { Icon(icon, label) },
                label  = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = LMPrimary,
                    selectedTextColor   = LMPrimary,
                    unselectedIconColor = LMOnSurfaceVariant,
                    unselectedTextColor = LMOnSurfaceVariant,
                    indicatorColor      = LMPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun SongCard(
    song: SongItem,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = LMSurface),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(LMPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/seed/${song.id}/200/200")
                        .crossfade(true).build(),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title,  style = MaterialTheme.typography.titleMedium, color = LMOnBackground, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall,   color = LMOnSurfaceVariant)
            }
            trailing?.invoke() ?: Text(song.duration, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
        }
    }
}

// ================================================================
//  WELCOME SCREEN
// ================================================================

@Composable
fun WelcomeScreen(onComenzarClick: () -> Unit = {}, onYaTengoClick: () -> Unit = {}) {
    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(80.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppLogo(90)
                Spacer(Modifier.height(8.dp))
                Text("LyricMotion", style = MaterialTheme.typography.displayMedium, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Letras animadas", style = MaterialTheme.typography.headlineSmall, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                Text(
                    "Descubre tus canciones favoritas con\nestilos visuales únicos. Neón, Karaoke\ny Fade te esperan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LMOnSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
            Column(
                Modifier.fillMaxWidth().padding(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = onComenzarClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                    shape    = RoundedCornerShape(8.dp)
                ) { Text("COMENZAR", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }

                OutlinedButton(
                    onClick  = onYaTengoClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = LMPrimary),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, LMPrimary),
                    shape    = RoundedCornerShape(8.dp)
                ) { Text("YA TENGO CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
            }
        }
    }
}

// ================================================================
//  LOGIN SCREEN
// ================================================================

@Composable
fun LoginScreen(navController: NavController? = null, userManager: UserManager? = null) {
    val context  = LocalContext.current
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()
    val users    by um.getUsers.collectAsState(initial = emptyList())

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVis  by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var loading  by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = LMPrimary, unfocusedBorderColor      = LMSurfaceVariant,
        focusedLabelColor       = LMPrimary, unfocusedLabelColor       = LMOnSurfaceVariant,
        cursorColor             = LMPrimary,
        focusedTextColor        = LMOnBackground, unfocusedTextColor   = LMOnBackground,
        focusedContainerColor   = LMSurface, unfocusedContainerColor   = LMSurface
    )

    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            Text("Bienvenido", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Inicia sesión para continuar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))

            OutlinedTextField(
                email, { email = it; errorMsg = "" },
                label       = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine  = true, modifier = Modifier.fillMaxWidth(),
                shape       = RoundedCornerShape(8.dp), colors = tfColors
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                password, { password = it; errorMsg = "" },
                label        = { Text("CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = {
                    IconButton({ passVis = !passVis }) {
                        Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant)
                    }
                },
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine  = true, modifier = Modifier.fillMaxWidth(),
                shape       = RoundedCornerShape(8.dp), colors = tfColors
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = LMError)
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) { errorMsg = "Completa todos los campos"; return@Button }
                    scope.launch {
                        loading = true
                        val user = um.loginUser(users, email.trim(), password)
                        loading  = false
                        if (user != null) navController?.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                        else errorMsg = "Correo o contraseña incorrectos"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !loading,
                colors   = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                shape    = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(color = LMOnPrimary, modifier = Modifier.size(20.dp))
                else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            Spacer(Modifier.weight(1f))
            Row(
                Modifier.fillMaxWidth().padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = { navController?.navigate(Screen.Register.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Regístrate", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  REGISTER SCREEN
// ================================================================

@Composable
fun RegisterScreen(navController: NavController? = null, userManager: UserManager? = null) {
    val context  = LocalContext.current
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()
    val users    by um.getUsers.collectAsState(initial = emptyList())

    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var passVis  by remember { mutableStateOf(false) }
    var confVis  by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var loading  by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = LMPrimary, unfocusedBorderColor      = LMSurfaceVariant,
        focusedLabelColor       = LMPrimary, unfocusedLabelColor       = LMOnSurfaceVariant,
        cursorColor             = LMPrimary,
        focusedTextColor        = LMOnBackground, unfocusedTextColor   = LMOnBackground,
        focusedContainerColor   = LMSurface, unfocusedContainerColor   = LMSurface
    )

    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu cuenta para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(name, { name = it; errorMsg = "" }, label = { Text("NOMBRE") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = LMOnSurfaceVariant) },
                singleLine  = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(email, { email = it; errorMsg = "" }, label = { Text("EMAIL") },
                leadingIcon     = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine  = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it; errorMsg = "" }, label = { Text("CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ passVis = !passVis }) { Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant) } },
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine  = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(confirm, { confirm = it; errorMsg = "" }, label = { Text("CONFIRMAR CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ confVis = !confVis }) { Icon(if (confVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant) } },
                visualTransformation = if (confVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine  = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = LMError, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() -> errorMsg = "Completa todos los campos"
                        password != confirm -> errorMsg = "Las contraseñas no coinciden"
                        password.length < 6 -> errorMsg = "Mínimo 6 caracteres"
                        else -> scope.launch {
                            loading = true
                            val newUser = User(UUID.randomUUID().toString(), name.trim(), email.trim().lowercase(), password)
                            val ok = um.registerUser(users, newUser)
                            if (ok) {
                                um.loginUser(users + newUser, newUser.email, newUser.password)
                                loading = false
                                navController?.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                            } else { loading = false; errorMsg = "Este correo ya está registrado" }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !loading,
                colors   = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                shape    = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(color = LMOnPrimary, modifier = Modifier.size(20.dp))
                else Text("CREAR CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = { navController?.navigate(Screen.Login.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Inicia sesión", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  HOME SCREEN
// ================================================================

@Composable
fun HomeScreen(navController: NavController? = null) {
    var searchQuery   by remember { mutableStateOf("") }
    var currentState  by remember { mutableStateOf(SearchState.IDLE) }
    var filteredSongs by remember { mutableStateOf(emptyList<SongItem>()) }

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("LyricMotion", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    if (q.isBlank()) { currentState = SearchState.IDLE; filteredSongs = emptyList() }
                    else {
                        val r = sampleSongs.filter { it.title.contains(q, true) || it.artist.contains(q, true) }
                        filteredSongs = r
                        currentState  = if (r.isEmpty()) SearchState.EMPTY else SearchState.RESULTS
                    }
                },
                placeholder  = { Text("Buscar canciones o artistas...", color = LMOnSurfaceVariant) },
                leadingIcon  = { Icon(Icons.Default.Search, null, tint = LMOnSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) IconButton({
                        searchQuery  = ""
                        currentState = SearchState.IDLE
                        filteredSongs = emptyList()
                    }) { Icon(Icons.Default.Close, null, tint = LMOnSurfaceVariant) }
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor    = LMPrimary,      unfocusedBorderColor  = LMSurfaceVariant,
                    focusedTextColor      = LMOnBackground, unfocusedTextColor    = LMOnBackground,
                    focusedContainerColor = LMSurface,      unfocusedContainerColor = LMSurface
                )
            )
            Spacer(Modifier.height(16.dp))

            when (currentState) {
                SearchState.IDLE -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text(
                                "🔥 Destacadas",
                                style      = MaterialTheme.typography.titleMedium,
                                color      = LMOnBackground,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(featuredSongs) { song ->
                            SongCard(song, onClick = { navController?.navigate(Screen.LyricsViewer.createRoute(song.id)) })
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Busca más canciones arriba ↑",
                                style     = MaterialTheme.typography.bodySmall,
                                color     = LMOnSurfaceVariant.copy(alpha = 0.6f),
                                modifier  = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                SearchState.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LMPrimary)
                }
                SearchState.ERROR -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ErrorOutline, null, tint = LMError, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Error al cargar resultados.\nIntenta nuevamente.", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                }
                SearchState.EMPTY -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.MusicOff, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No se encontraron resultados", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                }
                SearchState.RESULTS -> {
                    Text("Resultados", style = MaterialTheme.typography.titleMedium, color = LMOnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredSongs) { song ->
                            SongCard(song, onClick = { navController?.navigate(Screen.LyricsViewer.createRoute(song.id)) })
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
//  LYRICS VIEWER SCREEN
// ================================================================

@Composable
fun LyricsViewerScreen(
    song: SongItem           = sampleSongs[0],
    settings: AppSettings    = AppSettings(),
    isSaved: Boolean         = false,
    onToggleSave: () -> Unit = {},
    onBackClick: () -> Unit  = {}
) {
    val initialStyle = remember(settings.defaultStyleIndex) {
        when (settings.defaultStyleIndex) { 1 -> LyricStyle.NEON; 2 -> LyricStyle.KARAOKE; else -> LyricStyle.FADE }
    }
    var style by remember { mutableStateOf(initialStyle) }

    val bgColor     = when (style) { LyricStyle.NEON -> NeonBackground; LyricStyle.KARAOKE -> KaraokeBackground; LyricStyle.FADE -> FadeBackground }
    val lyricsColor = when (style) { LyricStyle.NEON -> NeonPrimary;    LyricStyle.KARAOKE -> KaraokeText;       LyricStyle.FADE -> FadeText }
    val animDuration = (500 / settings.animationSpeed).toInt().coerceAtLeast(100)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Row(
                Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LMOnBackground) }
                Column(Modifier.weight(1f)) {
                    Text(song.title,  style = MaterialTheme.typography.titleLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall,  color = LMOnSurfaceVariant)
                }
            }
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LyricStyle.entries.forEach { s ->
                        val sel = style == s
                        FilterChip(
                            selected = sel, onClick = { style = s },
                            label    = { Text(s.name, fontSize = 11.sp) },
                            colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurface, labelColor = LMOnSurfaceVariant),
                            border   = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FloatingActionButton(
                        onClick        = { onToggleSave() },
                        containerColor = if (isSaved) LMPrimary else LMSurface,
                        contentColor   = if (isSaved) LMOnPrimary else LMOnSurfaceVariant,
                        modifier       = Modifier.size(52.dp)
                    ) { Icon(if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null) }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.size(180.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/seed/${song.id}/400/400").crossfade(true).build(),
                    contentDescription = "Cover art", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState  = style,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(animDuration)) +
                            slideInVertically(animationSpec = tween(animDuration)) { it / 4 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(animDuration / 2)) +
                                    slideOutVertically(animationSpec = tween(animDuration)) { -it / 4 }
                        )
                },
                modifier = Modifier.weight(1f),
                label    = "lyrics_style_anim"
            ) { currentStyle ->
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = song.lyrics,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize   = settings.fontSize.sp,
                            lineHeight = (settings.fontSize * 1.7f).sp,
                            fontFamily = lyricFontFamily(currentStyle),
                            fontStyle  = if (currentStyle == LyricStyle.FADE) FontStyle.Italic else FontStyle.Normal
                        ),
                        color     = lyricsColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// ================================================================
//  SAVED SCREEN
// ================================================================

@Composable
fun SavedScreen(
    navController: NavController? = null,
    savedSongs: List<SongItem>    = emptyList(),
    onRemove: (String) -> Unit    = {}
) {
    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text("Letras Guardadas", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (savedSongs.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.BookmarkBorder, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("No tienes letras guardadas", style = MaterialTheme.typography.headlineSmall, color = LMOnSurfaceVariant, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text("Busca una canción y guárdala\npara verla aquí", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { navController?.navigate(Screen.Home.route) },
                        colors  = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                        shape   = RoundedCornerShape(8.dp)
                    ) { Text("BUSCAR CANCIONES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(savedSongs, key = { it.id }) { song ->
                        SongCard(
                            song     = song,
                            onClick  = { navController?.navigate(Screen.LyricsViewer.createRoute(song.id)) },
                            trailing = {
                                IconButton(onClick = { onRemove(song.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = LMError)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
//  SETTINGS SCREEN
// ================================================================

@Composable
fun SettingsScreen(
    navController: NavController? = null,
    userManager: UserManager?     = null,
    userEmail: String             = ""
) {
    val context  = LocalContext.current
    val sm       = remember { SettingsManager(context) }
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()

    // Ajustes cargados por usuario
    val settings by remember(userEmail) {
        if (userEmail.isNotEmpty()) sm.getSettingsForUser(userEmail)
        else flowOf(AppSettings())
    }.collectAsState(initial = AppSettings())

    var selStyle  by remember(settings.defaultStyleIndex) { mutableStateOf(settings.defaultStyleIndex) }
    var fontSize  by remember(settings.fontSize)          { mutableStateOf(settings.fontSize) }
    var animSpeed by remember(settings.animationSpeed)    { mutableStateOf(settings.animationSpeed) }
    var autoPlay  by remember(settings.autoPlay)          { mutableStateOf(settings.autoPlay) }
    var showAbout by remember { mutableStateOf(false) }

    val styles = listOf("Fade", "Neón", "Karaoke")

    val previewStyle = when (selStyle) { 1 -> LyricStyle.NEON; 2 -> LyricStyle.KARAOKE; else -> LyricStyle.FADE }
    val previewColor = when (previewStyle) { LyricStyle.NEON -> NeonPrimary; LyricStyle.KARAOKE -> KaraokeText; LyricStyle.FADE -> FadeText }
    val previewBg    = when (previewStyle) { LyricStyle.NEON -> NeonBackground; LyricStyle.KARAOKE -> KaraokeBackground; LyricStyle.FADE -> FadeBackground }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title  = { Text("Acerca de LyricMotion") },
            text   = { Text("LyricMotion v1.0.0\nDesarrollado con Jetpack Compose.\n\nVisualiza letras animadas con estilos Neón, Karaoke y Fade.") },
            confirmButton = { TextButton({ showAbout = false }) { Text("Cerrar") } }
        )
    }

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = LMPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {

            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMPrimary), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppLogo(48)
                        Column {
                            Text("LyricMotion", style = MaterialTheme.typography.titleLarge, color = LMOnPrimary, fontWeight = FontWeight.Bold)
                            Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = LMOnPrimary.copy(0.7f))
                        }
                    }
                }
            }

            item { SectionHeader("Visualización") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Estilo predeterminado", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            styles.forEachIndexed { i, label ->
                                val sel = selStyle == i
                                FilterChip(
                                    sel, {
                                        selStyle = i
                                        scope.launch { sm.saveDefaultStyle(userEmail, i) }
                                    },
                                    label  = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurfaceVariant, labelColor = LMOnSurfaceVariant),
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(previewBg).padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Look at the stars\nLook how they shine for you",
                                color      = previewColor,
                                fontSize   = fontSize.sp,
                                fontFamily = lyricFontFamily(previewStyle),
                                fontStyle  = if (previewStyle == LyricStyle.FADE) FontStyle.Italic else FontStyle.Normal,
                                textAlign  = TextAlign.Center,
                                lineHeight = (fontSize * 1.7f).sp
                            )
                        }
                    }
                }
            }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TextFields, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tamaño de letra", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text("${fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            fontSize, { fontSize = it; scope.launch { sm.saveFontSize(userEmail, it) } },
                            valueRange = 12f..28f, steps = 7,
                            colors     = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("12sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("28sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            item { SectionHeader("Animaciones") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Velocidad de animación", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text(String.format(Locale.getDefault(), "%.1f x", animSpeed), style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            animSpeed, { animSpeed = it; scope.launch { sm.saveAnimationSpeed(userEmail, it) } },
                            valueRange = 0.5f..2f, steps = 5,
                            colors     = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lento",  style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("Rápido", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SettCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Reproducción automática", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Text("Inicia los efectos al abrir una canción", style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                        }
                        Switch(
                            autoPlay, { autoPlay = it; scope.launch { sm.saveAutoPlay(userEmail, it) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = LMOnPrimary, checkedTrackColor = LMPrimary, uncheckedThumbColor = LMOnSurfaceVariant, uncheckedTrackColor = LMSurfaceVariant)
                        )
                    }
                }
            }

            item { SectionHeader("Cuenta") }

            item {
                SettCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = LMError, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Cerrar sesión", style = MaterialTheme.typography.bodyMedium, color = LMError, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        TextButton(onClick = {
                            scope.launch {
                                um.logout()
                                navController?.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                            }
                        }) { Text("Salir", color = LMError, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            item { SectionHeader("General") }

            item {
                SettCard {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Acerca de LyricMotion", style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAbout = true }) {
                                Icon(Icons.Default.ChevronRight, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettRow(Icons.Default.PrivacyTip, "Política de privacidad")
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettRow(Icons.Default.Description, "Términos de uso")
                    }
                }
            }
        }
    }
}

// ================================================================
//  HELPERS
// ================================================================

@Composable private fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = LMPrimary, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
}

@Composable private fun SettCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMSurface), shape = RoundedCornerShape(12.dp)) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable private fun SettRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

// ================================================================
//  PREVIEWS
// ================================================================

@Preview(showBackground = true, showSystemUi = true, name = "1 - Welcome")
@Composable fun PreviewWelcome() { LyricMotionTheme { WelcomeScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home")
@Composable fun PreviewHome() { LyricMotionTheme { HomeScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "5 - Lyrics")
@Composable fun PreviewLyrics() { LyricMotionTheme { LyricsViewerScreen(sampleSongs[0], AppSettings(), false, {}, {}) } }

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved")
@Composable fun PreviewSaved() { LyricMotionTheme { SavedScreen(savedSongs = sampleSongs.take(3)) } }

@Preview(showBackground = true, showSystemUi = true, name = "7 - Settings")
@Composable fun PreviewSettings() { LyricMotionTheme { SettingsScreen() } }