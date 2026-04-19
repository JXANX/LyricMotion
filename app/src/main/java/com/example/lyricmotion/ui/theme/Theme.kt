package com.lyricmotion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Color Schemes ────────────────────────────────────────────────

private val LMDarkColorScheme = darkColorScheme(
    primary             = LMPrimary,
    onPrimary           = LMOnPrimary,
    primaryContainer    = LMPrimaryContainer,
    onPrimaryContainer  = Purple80,
    secondary           = LMSecondary,
    onSecondary         = LMOnSecondary,
    secondaryContainer  = LMSecondaryVariant,
    onSecondaryContainer= LMSecondary,
    background          = LMBackground,
    onBackground        = LMOnBackground,
    surface             = LMSurface,
    onSurface           = LMOnSurface,
    surfaceVariant      = LMSurfaceVariant,
    onSurfaceVariant    = LMOnSurfaceVariant,
    error               = LMError,
    onError             = Color.White,
)

private val LMLightColorScheme = lightColorScheme(
    primary          = Purple40,
    onPrimary        = Color.White,
    primaryContainer = Purple80,
    secondary        = PurpleGrey40,
    background       = Color(0xFFF8F4FF),
    surface          = Color.White,
    onBackground     = Color(0xFF1A1A2E),
    onSurface        = Color(0xFF1A1A2E),
)

// ── Colores extra por estilo de letras ───────────────────────────
data class LyricsStyleColors(
    val neonBackground    : Color = NeonBackground,
    val neonPrimary       : Color = NeonPrimary,
    val neonGlow          : Color = NeonGlow,
    val neonAccent        : Color = NeonAccent,
    val neonText          : Color = NeonText,
    val karaokeBackground : Color = KaraokeBackground,
    val karaokeHighlight  : Color = KaraokeHighlight,
    val karaokeText       : Color = KaraokeText,
    val karaokeDimmed     : Color = KaraokeDimmed,
    val fadeBackground    : Color = FadeBackground,
    val fadeText          : Color = FadeText,
    val fadeAccent        : Color = FadeAccent,
)

val LocalLyricsColors = staticCompositionLocalOf { LyricsStyleColors() }

// ── Espaciado — múltiplos de 8dp según el documento ─────────────
object LMSpacing {
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp   // tamaño mínimo de botón según el doc
    val xxxl = 64.dp
}

// ── Formas ───────────────────────────────────────────────────────
val LMShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// ── Theme principal ──────────────────────────────────────────────
@Composable
fun LyricMotionTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LMDarkColorScheme else LMLightColorScheme

    CompositionLocalProvider(
        LocalLyricsColors provides LyricsStyleColors()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = LMTypography,
            shapes      = LMShapes,
            content     = content
        )
    }
}

// ── Acceso conveniente desde cualquier Composable ────────────────
object LyricMotionTheme {
    val lyricsColors: LyricsStyleColors
        @Composable get() = LocalLyricsColors.current
}