package com.lyricmotion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyricmotion.data.SongItem
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface

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
