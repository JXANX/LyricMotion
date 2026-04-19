package com.lyricmotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyricmotion.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyricMotionTheme {
                WelcomeScreen()
            }
        }
    }
}

// ================================================================
//  DATOS DE EJEMPLO (compartidos entre pantallas)
// ================================================================

enum class SearchState { IDLE, LOADING, EMPTY, ERROR, RESULTS }
enum class LyricStyle  { NEON, KARAOKE, FADE }

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val lyrics: String = "Is this the real life?\nIs this just fantasy?\nCaught in a landslide\nNo escape from reality\nOpen your eyes\nLook up to the skies and see..."
)

val sampleSongs = listOf(
    SongItem("1", "Bohemian Rhapsody", "Queen",       "5:55"),
    SongItem("2", "Imagine",           "John Lennon", "3:07"),
    SongItem("3", "Mary Kills Men",    "Metric",      "4:12"),
    SongItem("4", "Somebody to Love",  "Queen",       "4:56"),
    SongItem("5", "Little App",        "Varios",      "3:30"),
)

// ================================================================
//  COMPONENTES REUTILIZABLES
// ================================================================

@Composable
private fun AppLogo(size: Int = 72) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(LMPrimaryVariant, LMPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "LyricMotion logo",
            tint = Color.White,
            modifier = Modifier.size((size * 0.44).dp)
        )
    }
}

@Composable
private fun LMBottomNav(selectedTab: Int = 0) {
    NavigationBar(
        containerColor = LMSurface,
        tonalElevation = 0.dp
    ) {
        listOf(
            Triple(Icons.Default.Home,           "Inicio",    0),
            Triple(Icons.Default.BookmarkBorder, "Guardadas", 1),
            Triple(Icons.Default.Settings,       "Ajustes",   2),
        ).forEach { (icon, label, idx) ->
            NavigationBarItem(
                selected = selectedTab == idx,
                onClick  = {},
                icon     = { Icon(icon, contentDescription = label) },
                label    = { Text(label) },
                colors   = NavigationBarItemDefaults.colors(
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
private fun SongCard(song: SongItem, onClick: () -> Unit = {}, trailing: @Composable (() -> Unit)? = null) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LMSurface),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LMPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = LMPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title,  style = MaterialTheme.typography.titleMedium, color = LMOnBackground, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall,   color = LMOnSurfaceVariant)
            }
            if (trailing != null) trailing()
            else Text(song.duration, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
        }
    }
}

// ================================================================
//  1. WELCOME SCREEN
// ================================================================

@Composable
fun WelcomeScreen(
    onComenzarClick: () -> Unit = {},
    onYaTengoClick:  () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize().background(LMBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(80.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLogo(size = 90)
                Spacer(Modifier.height(8.dp))
                Text("LyricMotion", style = MaterialTheme.typography.displayMedium, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Letras animadas",  style = MaterialTheme.typography.headlineSmall, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                Text(
                    "Descubre tus canciones favoritas con\nestilos visuales únicos. Neón, Karaoke\ny Fade te esperan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LMOnSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onComenzarClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                    shape  = RoundedCornerShape(8.dp)
                ) { Text("COMENZAR", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }

                OutlinedButton(
                    onClick = onYaTengoClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LMPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LMPrimary),
                    shape  = RoundedCornerShape(8.dp)
                ) { Text("YA TENGO CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
            }
        }
    }
}

// ================================================================
//  2. LOGIN SCREEN
// ================================================================

@Composable
fun LoginScreen(
    onLoginClick:    () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVis  by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor    = LMPrimary,       unfocusedBorderColor  = LMSurfaceVariant,
        focusedLabelColor     = LMPrimary,       unfocusedLabelColor   = LMOnSurfaceVariant,
        cursorColor           = LMPrimary,
        focusedTextColor      = LMOnBackground,  unfocusedTextColor    = LMOnBackground,
        focusedContainerColor = LMSurface,       unfocusedContainerColor = LMSurface
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

            OutlinedTextField(email, { email = it }, label = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), colors = tfColors)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(password, { password = it }, label = { Text("CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ passVis = !passVis }) {
                    Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant)
                }},
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), colors = tfColors)

            Spacer(Modifier.height(32.dp))
            Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                shape = RoundedCornerShape(8.dp)) {
                Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(bottom = 40.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = onRegisterClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Regístrate", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  3. REGISTER SCREEN
// ================================================================

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit = {},
    onLoginClick:    () -> Unit = {}
) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var passVis  by remember { mutableStateOf(false) }
    var confVis  by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LMPrimary, unfocusedBorderColor = LMSurfaceVariant,
        focusedLabelColor  = LMPrimary, unfocusedLabelColor  = LMOnSurfaceVariant,
        cursorColor = LMPrimary,
        focusedTextColor = LMOnBackground, unfocusedTextColor = LMOnBackground,
        focusedContainerColor = LMSurface, unfocusedContainerColor = LMSurface
    )

    Box(Modifier.fillMaxSize().background(LMBackground)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(72.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu cuenta para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(name, { name = it }, label = { Text("NOMBRE") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = LMOnSurfaceVariant) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(email, { email = it }, label = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(password, { password = it }, label = { Text("CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ passVis = !passVis }) {
                    Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant)
                }},
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(confirm, { confirm = it }, label = { Text("CONFIRMAR CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { IconButton({ confVis = !confVis }) {
                    Icon(if (confVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant)
                }},
                visualTransformation = if (confVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = tfColors)
            Spacer(Modifier.height(28.dp))

            Button(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                shape = RoundedCornerShape(8.dp)) {
                Text("CREAR CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(bottom = 40.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Inicia sesión", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================================================================
//  4. HOME SCREEN
// ================================================================

@Composable
fun HomeScreen(
    searchState: SearchState = SearchState.IDLE,
    songs:       List<SongItem> = emptyList(),
    onSongClick: (SongItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("LyricMotion", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { LMBottomNav(selectedTab = 0) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar canciones o artistas...", color = LMOnSurfaceVariant) },
                leadingIcon  = { Icon(Icons.Default.Search, null, tint = LMOnSurfaceVariant) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton({ searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = LMOnSurfaceVariant) } },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LMPrimary, unfocusedBorderColor = LMSurfaceVariant,
                    focusedTextColor = LMOnBackground, unfocusedTextColor = LMOnBackground,
                    focusedContainerColor = LMSurface, unfocusedContainerColor = LMSurface
                )
            )
            Spacer(Modifier.height(16.dp))

            when (searchState) {
                SearchState.IDLE -> {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Search, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Busca tus canciones favoritas", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("Escribe el nombre de una canción\no artista para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    }
                }
                SearchState.LOADING -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = LMPrimary) }
                }
                SearchState.ERROR -> {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.ErrorOutline, null, tint = LMError, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Error al cargar los resultados.\nIntenta nuevamente.", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
                SearchState.EMPTY -> {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.MusicOff, null, tint = LMOnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No se encontraron resultados", style = MaterialTheme.typography.bodyLarge, color = LMOnSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("Intenta con otro nombre o artista", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    }
                }
                SearchState.RESULTS -> {
                    Text("Resultados", style = MaterialTheme.typography.titleMedium, color = LMOnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(songs) { song -> SongCard(song = song, onClick = { onSongClick(song) }) }
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
fun LyricsViewerScreen(
    song:        SongItem = sampleSongs[0],
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    isSaved:     Boolean = false
) {
    var currentStyle by remember { mutableStateOf(LyricStyle.FADE) }

    // Colores según estilo — fiel al mockup
    val bgColor = when (currentStyle) {
        LyricStyle.NEON    -> NeonBackground
        LyricStyle.KARAOKE -> KaraokeBackground
        LyricStyle.FADE    -> FadeBackground
    }
    val lyricsColor = when (currentStyle) {
        LyricStyle.NEON    -> NeonPrimary
        LyricStyle.KARAOKE -> KaraokeText
        LyricStyle.FADE    -> FadeText
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            // TopBar con flecha atrás + título canción
            Row(
                modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = LMOnBackground)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title,  style = MaterialTheme.typography.titleLarge,  color = LMOnBackground, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                }
            }
        },
        bottomBar = {
            // Selector de estilos + botón guardar
            Column(
                modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Chips de estilo — NEON / KARAOKE / FADE
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LyricStyle.entries.forEach { style ->
                        val isSelected = currentStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick  = { currentStyle = style },
                            label    = { Text(style.name, fontSize = 11.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LMPrimary,
                                selectedLabelColor     = LMOnPrimary,
                                containerColor         = LMSurface,
                                labelColor             = LMOnSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled          = true,
                                selected         = isSelected,
                                selectedBorderColor = LMPrimary,
                                borderColor      = LMSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // FAB guardar centrado
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FloatingActionButton(
                        onClick = onSaveClick,
                        containerColor = if (isSaved) LMPrimary else LMSurface,
                        contentColor   = if (isSaved) LMOnPrimary else LMOnSurfaceVariant,
                        modifier       = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar"
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Letras de la canción
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text  = song.lyrics,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = 18.sp,
                    lineHeight = 32.sp
                ),
                color     = lyricsColor,
                textAlign = TextAlign.Start,
                modifier  = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ================================================================
//  6. SAVED SCREEN
// ================================================================

@Composable
fun SavedScreen(
    savedSongs:     List<SongItem> = emptyList(),
    onSongClick:    (SongItem) -> Unit = {},
    onDeleteClick:  (SongItem) -> Unit = {}
) {
    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(Modifier.fillMaxWidth().background(LMBackground).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text("Letras Guardadas", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { LMBottomNav(selectedTab = 1) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (savedSongs.isEmpty()) {
                // Empty state — fiel al mockup
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint     = LMOnSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "No tienes letras guardadas",
                        style     = MaterialTheme.typography.headlineSmall,
                        color     = LMOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Busca una canción y guárdala\npara verla aquí",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = LMOnSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                        shape  = RoundedCornerShape(8.dp)
                    ) {
                        Text("BUSCAR CANCIONES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            } else {
                // Lista de guardadas
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(savedSongs) { song ->
                        SongCard(
                            song    = song,
                            onClick = { onSongClick(song) },
                            trailing = {
                                IconButton(onClick = { onDeleteClick(song) }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = LMOnSurfaceVariant)
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
//  7. SETTINGS SCREEN
// ================================================================

@Composable
fun SettingsScreen() {
    var selectedStyle   by remember { mutableStateOf(0) }
    var fontSize        by remember { mutableStateOf(16f) }
    var animSpeed       by remember { mutableStateOf(1f) }
    var autoPlay        by remember { mutableStateOf(true) }

    val styles = listOf("Fade", "Neón", "Karaoke")

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            Box(
                Modifier.fillMaxWidth().background(LMBackground)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = LMPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = { LMBottomNav(selectedTab = 2) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {

            // ── Banner de la app ─────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LMPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppLogo(size = 48)
                        Column {
                            Text("LyricMotion", style = MaterialTheme.typography.titleLarge, color = LMOnPrimary, fontWeight = FontWeight.Bold)
                            Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = LMOnPrimary.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // ── Sección: Visualización ───────────────────────────
            item { SettingsSectionHeader(title = "Visualización") }

            // Estilo predeterminado
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Estilo predeterminado", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            styles.forEachIndexed { idx, label ->
                                val selected = selectedStyle == idx
                                FilterChip(
                                    selected = selected,
                                    onClick  = { selectedStyle = idx },
                                    label    = { Text(label, fontSize = 12.sp) },
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LMPrimary,
                                        selectedLabelColor     = LMOnPrimary,
                                        containerColor         = LMSurfaceVariant,
                                        labelColor             = LMOnSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true, selected = selected,
                                        selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Tamaño de fuente
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TextFields, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tamaño de letra", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text("${fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 12f..28f,
                            steps = 7,
                            colors = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("12sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("28sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            // ── Sección: Animaciones ─────────────────────────────
            item { SettingsSectionHeader(title = "Animaciones") }

            // Velocidad
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Velocidad", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text("${String.format("%.1f", animSpeed)}x", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = animSpeed,
                            onValueChange = { animSpeed = it },
                            valueRange = 0.5f..2f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lento", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("Rápido", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            // Reproducción automática
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Reproducción automática", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Text("Inicia los efectos al abrir una canción", style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                        }
                        Switch(
                            checked = autoPlay,
                            onCheckedChange = { autoPlay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LMOnPrimary,
                                checkedTrackColor = LMPrimary,
                                uncheckedThumbColor = LMOnSurfaceVariant,
                                uncheckedTrackColor = LMSurfaceVariant
                            )
                        )
                    }
                }
            }

            // ── Sección: General ─────────────────────────────────
            item { SettingsSectionHeader(title = "General") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SettingsRowItem(icon = Icons.Default.Info,         label = "Acerca de LyricMotion")
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettingsRowItem(icon = Icons.Default.PrivacyTip,  label = "Política de privacidad")
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettingsRowItem(icon = Icons.Default.Description,  label = "Términos de uso")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LMPrimary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LMSurface),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SettingsRowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

// ================================================================
//  PREVIEWS
// ================================================================

@Preview(showBackground = true, showSystemUi = true, name = "1 - Welcome Screen")
@Composable
fun PreviewWelcome() { LyricMotionTheme { WelcomeScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "2 - Login Screen")
@Composable
fun PreviewLogin() { LyricMotionTheme { LoginScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "3 - Register Screen")
@Composable
fun PreviewRegister() { LyricMotionTheme { RegisterScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Idle")
@Composable
fun PreviewHomeIdle() { LyricMotionTheme { HomeScreen(searchState = SearchState.IDLE) } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Loading")
@Composable
fun PreviewHomeLoading() { LyricMotionTheme { HomeScreen(searchState = SearchState.LOADING) } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Error")
@Composable
fun PreviewHomeError() { LyricMotionTheme { HomeScreen(searchState = SearchState.ERROR) } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Empty")
@Composable
fun PreviewHomeEmpty() { LyricMotionTheme { HomeScreen(searchState = SearchState.EMPTY) } }

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home Results")
@Composable
fun PreviewHomeResults() { LyricMotionTheme { HomeScreen(searchState = SearchState.RESULTS, songs = sampleSongs) } }

@Preview(showBackground = true, showSystemUi = true, name = "5 - Lyrics Neon")
@Composable
fun PreviewLyricsNeon() { LyricMotionTheme { LyricsViewerScreen(song = sampleSongs[0]) } }

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved Empty")
@Composable
fun PreviewSavedEmpty() { LyricMotionTheme { SavedScreen(savedSongs = emptyList()) } }

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved With Songs")
@Composable
fun PreviewSavedWithSongs() { LyricMotionTheme { SavedScreen(savedSongs = sampleSongs) } }

@Preview(showBackground = true, showSystemUi = true, name = "7 - Settings")
@Composable
fun PreviewSettings() { LyricMotionTheme { SettingsScreen() } }