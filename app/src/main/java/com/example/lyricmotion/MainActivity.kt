package com.example.lyricmotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Src
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lyricmotion.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

// ================================================================
//  MAIN ACTIVITY
// ================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyricMotionTheme {
                LyricMotionApp()
            }
        }
    }
}

// ================================================================
//  APP ROOT — NavHost centralizado
// ================================================================

@Composable
fun LyricMotionApp() {
    val navController = rememberNavController()
    val context       = LocalContext.current
    val userManager   = remember { UserManager(context) }
    val isLoggedIn    by userManager.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController    = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Welcome.route
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
            SavedScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, userManager = userManager)
        }
        composable(
            route     = Screen.LyricsViewer.route,
            arguments = listOf(navArgument(Screen.LyricsViewer.ARG_SONG_ID) { type = NavType.StringType })
        ) { backStack ->
            val songId = backStack.arguments?.getString(Screen.LyricsViewer.ARG_SONG_ID) ?: ""
            val song   = sampleSongs.find { it.id == songId } ?: sampleSongs[0]
            LyricsViewerScreen(song = song, onBackClick = { navController.popBackStack() })
        }
    }
}

// ================================================================
//  DATOS DE EJEMPLO
// ================================================================

enum class SearchState { IDLE, LOADING, EMPTY, ERROR, RESULTS }
enum class LyricStyle  { NEON, KARAOKE, FADE }

data class SongItem(
    val id:       String,
    val title:    String,
    val artist:   String,
    val duration: String,
    val lyrics:   String = "Is this the real life?\nIs this just fantasy?\nCaught in a landslide\nNo escape from reality\nOpen your eyes\nLook up to the skies and see..."
)

val sampleSongs = listOf(
    SongItem("1", "Bohemian Rhapsody", "Queen",       "5:55"),
    SongItem("2", "Imagine",           "John Lennon", "3:07"),
    SongItem("3", "Mary Kills Men",    "Metric",      "4:12"),
    SongItem("4", "Somebody to Love",  "Queen",       "4:56"),
    SongItem("5", "Little App",        "Varios",      "3:30"),
)

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
        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size((size * 0.44).dp))
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
                        launchSingleTop = true; restoreState = true
                    }
                },
                icon   = { Icon(icon, label) },
                label  = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LMPrimary, selectedTextColor = LMPrimary,
                    unselectedIconColor = LMOnSurfaceVariant, unselectedTextColor = LMOnSurfaceVariant,
                    indicatorColor = LMPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun SongCard(song: SongItem, onClick: () -> Unit = {}, trailing: @Composable (() -> Unit)? = null) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMSurface), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(LMPrimary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MusicNote, null, tint = LMPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title,  style = MaterialTheme.typography.titleMedium, color = LMOnBackground, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
            }
            trailing?.invoke() ?: Text(song.duration, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
        }
    }
}

// ================================================================
//  1. WELCOME SCREEN
// ================================================================

@Composable
fun WelcomeScreen(onComenzarClick: () -> Unit = {}, onYaTengoClick: () -> Unit = {}) {
    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Spacer(Modifier.height(80.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLogo(90)
                Spacer(Modifier.height(8.dp))
                Text("LyricMotion", style = MaterialTheme.typography.displayMedium, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Letras animadas", style = MaterialTheme.typography.headlineSmall, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                Text("Descubre tus canciones favoritas con\nestilos visuales únicos. Neón, Karaoke\ny Fade te esperan.",
                    style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center, lineHeight = 20.sp)
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onComenzarClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("COMENZAR", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
                OutlinedButton(onClick = onYaTengoClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = LMPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LMPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("YA TENGO CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }
        }
    }
}

// ================================================================
//  2. LOGIN SCREEN — con DataStore real
// ================================================================

@Composable
fun LoginScreen(navController: NavController? = null, userManager: UserManager? = null) {
    val context   = LocalContext.current
    val um        = userManager ?: remember { UserManager(context) }
    val scope     = rememberCoroutineScope()
    val users     by um.getUsers.collectAsState(initial = emptyList())

    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var passVis   by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }
    var loading   by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LMPrimary, unfocusedBorderColor = LMSurfaceVariant,
        focusedLabelColor = LMPrimary, unfocusedLabelColor = LMOnSurfaceVariant, cursorColor = LMPrimary,
        focusedTextColor = LMOnBackground, unfocusedTextColor = LMOnBackground,
        focusedContainerColor = LMSurface, unfocusedContainerColor = LMSurface
    )

    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(72.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            Text("Bienvenido", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Inicia sesión para continuar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))

            OutlinedTextField(email, { email = it; errorMsg = "" }, label = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(password, { password = it; errorMsg = "" }, label = { Text("CONTRASEÑA") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ passVis = !passVis }) { Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant) } },
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)

            if (errorMsg.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = LMError) }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) { errorMsg = "Completa todos los campos"; return@Button }
                    scope.launch {
                        loading = true
                        val user = um.loginUser(users, email.trim(), password)
                        loading = false
                        if (user != null) navController?.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                        else errorMsg = "Correo o contraseña incorrectos"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary), shape = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(color = LMOnPrimary, modifier = Modifier.size(20.dp))
                else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(bottom = 40.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = { navController?.navigate(Screen.Register.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Regístrate", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  3. REGISTER SCREEN — con DataStore real
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
        focusedBorderColor = LMPrimary, unfocusedBorderColor = LMSurfaceVariant,
        focusedLabelColor = LMPrimary, unfocusedLabelColor = LMOnSurfaceVariant, cursorColor = LMPrimary,
        focusedTextColor = LMOnBackground, unfocusedTextColor = LMOnBackground,
        focusedContainerColor = LMSurface, unfocusedContainerColor = LMSurface
    )

    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(72.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu cuenta para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(name, { name = it; errorMsg = "" }, label = { Text("NOMBRE") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = LMOnSurfaceVariant) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(email, { email = it; errorMsg = "" }, label = { Text("EMAIL") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it; errorMsg = "" }, label = { Text("CONTRASEÑA") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ passVis = !passVis }) { Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant) } },
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(confirm, { confirm = it; errorMsg = "" }, label = { Text("CONFIRMAR CONTRASEÑA") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ confVis = !confVis }) { Icon(if (confVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant) } },
                visualTransformation = if (confVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)

            if (errorMsg.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = LMError, textAlign = TextAlign.Center) }
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
                colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary), shape = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(color = LMOnPrimary, modifier = Modifier.size(20.dp))
                else Text("CREAR CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 40.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = { navController?.navigate(Screen.Login.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Inicia sesión", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  4. HOME SCREEN — búsqueda funcional
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
                        filteredSongs = r; currentState = if (r.isEmpty()) SearchState.EMPTY else SearchState.RESULTS
                    }
                },
                placeholder  = { Text("Buscar canciones o artistas...", color = LMOnSurfaceVariant) },
                leadingIcon  = { Icon(Icons.Default.Search, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton({ searchQuery = ""; currentState = SearchState.IDLE; filteredSongs = emptyList() }) { Icon(Icons.Default.Close, null, tint = LMOnSurfaceVariant) } },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LMPrimary, unfocusedBorderColor = LMSurfaceVariant, focusedTextColor = LMOnBackground, unfocusedTextColor = LMOnBackground, focusedContainerColor = LMSurface, unfocusedContainerColor = LMSurface)
            )
            Spacer(Modifier.height(16.dp))

            when (currentState) {
                SearchState.IDLE -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Search, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Busca tus canciones favoritas", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("Escribe el nombre de una canción\no artista para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                }
                SearchState.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = LMPrimary) }
                SearchState.ERROR   -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ErrorOutline, null, tint = LMError, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Error al cargar resultados.\nIntenta nuevamente.", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                }
                SearchState.EMPTY   -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.MusicOff, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No se encontraron resultados", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("Intenta con otro nombre o artista", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
//  5. LYRICS VIEWER SCREEN
// ================================================================

@Composable
fun LyricsViewerScreen(song: SongItem = sampleSongs[0], onBackClick: () -> Unit = {}) {
    var style by remember { mutableStateOf(LyricStyle.FADE) }
    var saved by remember { mutableStateOf(false) }

    val bgColor     = when (style) { LyricStyle.NEON -> NeonBackground; LyricStyle.KARAOKE -> KaraokeBackground; LyricStyle.FADE -> FadeBackground }
    val lyricsColor = when (style) { LyricStyle.NEON -> NeonPrimary;    LyricStyle.KARAOKE -> KaraokeText;       LyricStyle.FADE -> FadeText }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Row(Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = LMOnBackground) }
                Column(Modifier.weight(1f)) {
                    Text(song.title,  style = MaterialTheme.typography.titleLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                }
            }
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LyricStyle.entries.forEach { s ->
                        val sel = style == s
                        FilterChip(selected = sel, onClick = { style = s }, label = { Text(s.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurface, labelColor = LMOnSurfaceVariant),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant),
                            modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FloatingActionButton(onClick = { saved = !saved }, containerColor = if (saved) LMPrimary else LMSurface, contentColor = if (saved) LMOnPrimary else LMOnSurfaceVariant, modifier = Modifier.size(52.dp)) {
                        Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null)
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(song.lyrics, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 32.sp), color = lyricsColor, textAlign = TextAlign.Start, modifier = Modifier.align(Alignment.Center))
        }
    }
}

// ================================================================
//  6. SAVED SCREEN
// ================================================================

@Composable
fun SavedScreen(navController: NavController? = null, savedSongs: List<SongItem> = emptyList()) {
    Scaffold(
        containerColor = LMBackground,
        topBar = { Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 16.dp)) { Text("Letras Guardadas", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold) } },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (savedSongs.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.BookmarkBorder, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("No tienes letras guardadas", style = MaterialTheme.typography.headlineSmall, color = LMOnSurfaceVariant, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text("Busca una canción y guárdala\npara verla aquí", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { navController?.navigate(Screen.Home.route) }, colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary), shape = RoundedCornerShape(8.dp)) {
                        Text("BUSCAR CANCIONES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)) {
                    items(savedSongs) { song ->
                        SongCard(song, onClick = { navController?.navigate(Screen.LyricsViewer.createRoute(song.id)) }, trailing = { IconButton({}) { Icon(Icons.Default.MoreVert, null, tint = LMOnSurfaceVariant) } })
                    }
                }
            }
        }
    }
}

// ================================================================
//  7. SETTINGS SCREEN — con DataStore real
// ================================================================

@Composable
fun SettingsScreen(navController: NavController? = null, userManager: UserManager? = null) {
    val context  = LocalContext.current
    val sm       = remember { SettingsManager(context) }
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()
    val settings by sm.getSettings.collectAsState(initial = AppSettings())

    var selStyle  by remember(settings.defaultStyleIndex) { mutableStateOf(settings.defaultStyleIndex) }
    var fontSize  by remember(settings.fontSize)          { mutableStateOf(settings.fontSize) }
    var animSpeed by remember(settings.animationSpeed)    { mutableStateOf(settings.animationSpeed) }
    var autoPlay  by remember(settings.autoPlay)          { mutableStateOf(settings.autoPlay) }

    val styles = listOf("Fade", "Neón", "Karaoke")

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
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {

            // Banner
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMPrimary), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppLogo(48)
                        Column { Text("LyricMotion", style = MaterialTheme.typography.titleLarge, color = LMOnPrimary, fontWeight = FontWeight.Bold); Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = LMOnPrimary.copy(0.7f)) }
                    }
                }
            }

            item { SectionHeader("Visualización") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Palette, null, tint = LMPrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Estilo predeterminado", style = MaterialTheme.typography.titleMedium, color = LMOnBackground) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            styles.forEachIndexed { i, label ->
                                val sel = selStyle == i
                                FilterChip(sel, { selStyle = i; scope.launch { sm.saveDefaultStyle(i) } }, label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurfaceVariant, labelColor = LMOnSurfaceVariant),
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.TextFields, null, tint = LMPrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Tamaño de letra", style = MaterialTheme.typography.titleMedium, color = LMOnBackground); Spacer(Modifier.weight(1f)); Text("${fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold) }
                        Slider(fontSize, { fontSize = it; scope.launch { sm.saveFontSize(it) } }, valueRange = 12f..28f, steps = 7, colors = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("12sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant); Text("28sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant) }
                    }
                }
            }

            item { SectionHeader("Animaciones") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Speed, null, tint = LMPrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Velocidad", style = MaterialTheme.typography.titleMedium, color = LMOnBackground); Spacer(Modifier.weight(1f)); Text("${String.format("%.1f", animSpeed)}x", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold) }
                        Slider(animSpeed, { animSpeed = it; scope.launch { sm.saveAnimationSpeed(it) } }, valueRange = 0.5f..2f, steps = 5, colors = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Lento", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant); Text("Rápido", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant) }
                    }
                }
            }

            item {
                SettCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, null, tint = LMPrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) { Text("Reproducción automática", style = MaterialTheme.typography.titleMedium, color = LMOnBackground); Text("Inicia los efectos al abrir una canción", style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant) }
                        Switch(autoPlay, { autoPlay = it; scope.launch { sm.saveAutoPlay(it) } }, colors = SwitchDefaults.colors(checkedThumbColor = LMOnPrimary, checkedTrackColor = LMPrimary, uncheckedThumbColor = LMOnSurfaceVariant, uncheckedTrackColor = LMSurfaceVariant))
                    }
                }
            }

            item { SectionHeader("Cuenta") }

            item {
                SettCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, null, tint = LMError, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
                        Text("Cerrar sesión", style = MaterialTheme.typography.bodyMedium, color = LMError, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        TextButton(onClick = { scope.launch { um.logout(); navController?.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } } } }) {
                            Text("Salir", color = LMError, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { SectionHeader("General") }
            item {
                SettCard {
                    Column {
                        SettRow(Icons.Default.Info, "Acerca de LyricMotion")
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

@Composable private fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = LMPrimary, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
}
@Composable private fun SettCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMSurface), shape = RoundedCornerShape(12.dp)) { Box(Modifier.padding(16.dp)) { content() } }
}
@Composable private fun SettRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

// ================================================================
//  PREVIEWS
// ================================================================

@Preview(showBackground = true, showSystemUi = true, name = "1 - Welcome")
@Composable fun PreviewWelcome() { LyricMotionTheme { WelcomeScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "2 - Login")
@Composable fun PreviewLogin() { LyricMotionTheme { LoginScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "3 - Register")
@Composable fun PreviewRegister() { LyricMotionTheme { RegisterScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Idle")
@Composable fun PreviewHomeIdle() { LyricMotionTheme { HomeScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "5 - Lyrics Viewer")
@Composable fun PreviewLyrics() { LyricMotionTheme { LyricsViewerScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved Empty")
@Composable fun PreviewSavedEmpty() { LyricMotionTheme { SavedScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved Songs")
@Composable fun PreviewSavedSongs() { LyricMotionTheme { SavedScreen(savedSongs = sampleSongs) } }

@Preview(showBackground = true, showSystemUi = true, name = "7 - Settings")
@Composable fun PreviewSettings() { LyricMotionTheme { SettingsScreen() } }