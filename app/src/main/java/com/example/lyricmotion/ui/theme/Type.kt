package com.lyricmotion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía oficial según el documento de diseño:
 * → Roboto (fuente del sistema Android) para toda la app.
 * Unidades en SP para respetar accesibilidad del dispositivo.
 */
val LMTypography = Typography(
    displayLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    titleSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    bodyLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp)
)