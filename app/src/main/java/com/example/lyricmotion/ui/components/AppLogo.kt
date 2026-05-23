package com.lyricmotion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMPrimaryVariant

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
