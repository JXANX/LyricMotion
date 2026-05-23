package com.lyricmotion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyricmotion.Screen
import com.lyricmotion.data.SearchState
import com.lyricmotion.data.SongItem
import com.lyricmotion.data.featuredSongs
import com.lyricmotion.data.sampleSongs
import com.lyricmotion.ui.components.LMBottomNav
import com.lyricmotion.ui.components.SongCard
import com.lyricmotion.ui.theme.LMBackground
import com.lyricmotion.ui.theme.LMError
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface
import com.lyricmotion.ui.theme.LMSurfaceVariant
import com.lyricmotion.ui.theme.LyricMotionTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController? = null) {
    var searchQuery   by remember { mutableStateOf("") }
    var currentState  by remember { mutableStateOf(SearchState.IDLE) }
    var filteredSongs by remember { mutableStateOf(emptyList<SongItem>()) }

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("LyricMotion", style = MaterialTheme.typography.headlineMedium,
                        color = LMOnBackground, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LMBackground)
            )
        },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(4.dp))
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
                            Text("Destacadas", style = MaterialTheme.typography.titleMedium,
                                color = LMOnBackground, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp))
                        }
                        items(featuredSongs) { song ->
                            SongCard(song, onClick = { navController?.navigate(Screen.LyricsViewer.createRoute(song.id)) })
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Busca más canciones arriba ↑",
                                style = MaterialTheme.typography.bodySmall,
                                color = LMOnSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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

@Preview(showBackground = true, showSystemUi = true, name = "4 - Home")
@Composable
fun PreviewHome() { LyricMotionTheme { HomeScreen() } }
