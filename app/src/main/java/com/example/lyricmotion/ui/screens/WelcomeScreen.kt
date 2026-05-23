package com.lyricmotion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyricmotion.ui.components.AppLogo
import com.lyricmotion.ui.theme.LMBackground
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnPrimary
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LyricMotionTheme

@Composable
fun WelcomeScreen(onComenzarClick: () -> Unit = {}, onYaTengoClick: () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxSize()
            .background(LMBackground)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(48.dp))
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
                Modifier.fillMaxWidth().padding(bottom = 36.dp),
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
                ) { Text("NO TENGO CUENTA", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1 - Welcome")
@Composable
fun PreviewWelcome() { LyricMotionTheme { WelcomeScreen() } }
