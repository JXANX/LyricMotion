package com.lyricmotion.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyricmotion.SettingsManager
import com.lyricmotion.data.AppSettings
import com.lyricmotion.data.LyricStyle
import com.lyricmotion.data.lyricFontFamily
import com.lyricmotion.data.sampleSongs
import com.lyricmotion.ui.components.AnimatedLyricsPlayer
import com.lyricmotion.ui.theme.FadeBackground
import com.lyricmotion.ui.theme.FadeText
import com.lyricmotion.ui.theme.KaraokeBackground
import com.lyricmotion.ui.theme.KaraokeText
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnPrimary
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface
import com.lyricmotion.ui.theme.LMSurfaceVariant
import com.lyricmotion.ui.theme.LyricMotionTheme
import com.lyricmotion.ui.theme.NeonBackground
import com.lyricmotion.ui.theme.NeonPrimary
import com.lyricmotion.data.SongItem

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LMOnBackground)
                    }
                },
                title = {
                    Column {
                        Text(song.title, style = MaterialTheme.typography.titleLarge,
                            color = LMOnBackground, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LyricStyle.entries.forEach { s ->
                        val sel = style == s
                        FilterChip(
                            selected = sel, onClick = { style = s },
                            label    = { Text(s.name, fontSize = 11.sp) },
                            colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurface, labelColor = LMOnSurfaceVariant),
                            border   = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
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
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.size(160.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/seed/${song.id}/400/400").crossfade(true).build(),
                    contentDescription = "Cover art", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(16.dp))

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
                AnimatedLyricsPlayer(
                    lyrics     = song.lyrics,
                    fontSize   = settings.fontSize,
                    color      = lyricsColor,
                    fontFamily = lyricFontFamily(currentStyle),
                    fontStyle  = if (currentStyle == LyricStyle.FADE) FontStyle.Italic else FontStyle.Normal,
                    animSpeed  = settings.animationSpeed,
                    autoPlay   = settings.autoPlay,
                    modifier   = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "5 - Lyrics")
@Composable
fun PreviewLyrics() { LyricMotionTheme { LyricsViewerScreen(sampleSongs[0], AppSettings(), false, {}, {}) } }
