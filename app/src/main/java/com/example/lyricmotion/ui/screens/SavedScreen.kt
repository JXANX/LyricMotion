package com.lyricmotion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lyricmotion.Screen
import com.lyricmotion.data.SongItem
import com.lyricmotion.data.sampleSongs
import com.lyricmotion.ui.components.LMBottomNav
import com.lyricmotion.ui.components.SongCard
import com.lyricmotion.ui.theme.LMBackground
import com.lyricmotion.ui.theme.LMError
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnPrimary
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LyricMotionTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    navController: NavController? = null,
    savedSongs: List<SongItem>    = emptyList(),
    onRemove: (String) -> Unit    = {}
) {
    Scaffold(
        containerColor = LMBackground,
        topBar = {
            TopAppBar(
                title = { Text("Letras Guardadas", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LMBackground)
            )
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

@Preview(showBackground = true, showSystemUi = true, name = "6 - Saved")
@Composable
fun PreviewSaved() { LyricMotionTheme { SavedScreen(savedSongs = sampleSongs.take(3)) } }
