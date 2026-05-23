package com.lyricmotion.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedLyricsPlayer(
    lyrics:      String,
    fontSize:    Float,
    color:       Color,
    fontFamily:  FontFamily,
    fontStyle:   FontStyle,
    animSpeed:   Float,
    autoPlay:    Boolean,
    modifier:    Modifier = Modifier
) {
    val lines = remember(lyrics) {
        lyrics.split("\n").filter { it.isNotBlank() }
    }

    var currentLine   by remember { mutableIntStateOf(0) }
    var isPlaying     by remember { mutableStateOf(autoPlay) }
    val lineAlpha     = remember { Animatable(1f) }
    val lineOffsetY   = remember { Animatable(0f) }

    val lineDurationMs = remember(animSpeed) { (3000 / animSpeed).toLong().coerceAtLeast(800L) }
    val fadeDurationMs = remember(animSpeed) { (300 / animSpeed).toLong().coerceAtLeast(100L) }

    LaunchedEffect(isPlaying, lines, animSpeed) {
        if (!isPlaying || lines.isEmpty()) return@LaunchedEffect
        while (isPlaying) {
            delay(lineDurationMs)
            launch { lineAlpha.animateTo(0f, tween(fadeDurationMs.toInt())) }
            lineOffsetY.animateTo(-30f, tween(fadeDurationMs.toInt()))
            currentLine = (currentLine + 1) % lines.size
            lineOffsetY.snapTo(30f)
            launch { lineAlpha.animateTo(1f, tween(fadeDurationMs.toInt())) }
            lineOffsetY.animateTo(0f, tween(fadeDurationMs.toInt()))
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (lines.isNotEmpty()) {
                Text(
                    text      = lines[currentLine],
                    style     = MaterialTheme.typography.bodyLarge.copy(
                        fontSize   = (fontSize * 1.4f).sp,
                        lineHeight = (fontSize * 2f).sp,
                        fontFamily = fontFamily,
                        fontStyle  = fontStyle
                    ),
                    color     = color.copy(alpha = lineAlpha.value),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = lineOffsetY.value.dp)
                )
            }
        }

        // Puntos de progreso
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            val visibleCount = minOf(lines.size, 7)
            val halfVis      = visibleCount / 2
            val start        = (currentLine - halfVis).coerceAtLeast(0)
            val end          = (start + visibleCount).coerceAtMost(lines.size)

            (start until end).forEach { i ->
                val isActive = i == currentLine
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isActive) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (isActive) 1f else 0.35f))
                )
            }
        }

        // Líneas de contexto (anterior y siguiente)
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val prevLine = if (currentLine > 0) lines[currentLine - 1] else ""
            val nextLine = if (currentLine < lines.size - 1) lines[currentLine + 1] else ""
            if (prevLine.isNotEmpty()) {
                Text(prevLine, fontSize = (fontSize * 0.85f).sp, color = color.copy(alpha = 0.35f),
                    fontFamily = fontFamily, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            if (nextLine.isNotEmpty()) {
                Text(nextLine, fontSize = (fontSize * 0.85f).sp, color = color.copy(alpha = 0.35f),
                    fontFamily = fontFamily, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // Controles
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentLine = if (currentLine > 0) currentLine - 1 else lines.size - 1
            }) {
                Icon(Icons.Default.SkipPrevious, "Anterior", tint = color, modifier = Modifier.size(32.dp))
            }
            FloatingActionButton(
                onClick        = { isPlaying = !isPlaying },
                containerColor = color.copy(alpha = 0.2f),
                contentColor   = color,
                modifier       = Modifier.size(52.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = {
                currentLine = if (currentLine < lines.size - 1) currentLine + 1 else 0
            }) {
                Icon(Icons.Default.SkipNext, "Siguiente", tint = color, modifier = Modifier.size(32.dp))
            }
        }
    }
}