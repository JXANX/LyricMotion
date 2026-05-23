package com.lyricmotion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lyricmotion.Screen
import com.lyricmotion.SettingsManager
import com.lyricmotion.UserManager
import com.lyricmotion.data.AppSettings
import com.lyricmotion.data.LyricStyle
import com.lyricmotion.data.lyricFontFamily
import com.lyricmotion.ui.components.AppLogo
import com.lyricmotion.ui.components.LMBottomNav
import com.lyricmotion.ui.theme.FadeBackground
import com.lyricmotion.ui.theme.FadeText
import com.lyricmotion.ui.theme.KaraokeBackground
import com.lyricmotion.ui.theme.KaraokeText
import com.lyricmotion.ui.theme.LMBackground
import com.lyricmotion.ui.theme.LMError
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnPrimary
import com.lyricmotion.ui.theme.LMOnSurface
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface
import com.lyricmotion.ui.theme.LMSurfaceVariant
import com.lyricmotion.ui.theme.LyricMotionTheme
import com.lyricmotion.ui.theme.NeonBackground
import com.lyricmotion.ui.theme.NeonPrimary
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    userManager: UserManager?     = null,
    userEmail: String             = ""
) {
    val context  = LocalContext.current
    val sm       = remember { SettingsManager(context) }
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()

    val settings by remember(userEmail) {
        if (userEmail.isNotEmpty()) sm.getSettingsForUser(userEmail)
        else flowOf(AppSettings())
    }.collectAsState(initial = AppSettings())

    var selStyle  by remember(settings.defaultStyleIndex) { mutableStateOf(settings.defaultStyleIndex) }
    var fontSize  by remember(settings.fontSize)          { mutableStateOf(settings.fontSize) }
    var animSpeed by remember(settings.animationSpeed)    { mutableStateOf(settings.animationSpeed) }
    var autoPlay  by remember(settings.autoPlay)          { mutableStateOf(settings.autoPlay) }
    var showAbout by remember { mutableStateOf(false) }

    val styles = listOf("Fade", "Neón", "Karaoke")
    val previewStyle = when (selStyle) { 1 -> LyricStyle.NEON; 2 -> LyricStyle.KARAOKE; else -> LyricStyle.FADE }
    val previewColor = when (previewStyle) { LyricStyle.NEON -> NeonPrimary; LyricStyle.KARAOKE -> KaraokeText; LyricStyle.FADE -> FadeText }
    val previewBg    = when (previewStyle) { LyricStyle.NEON -> NeonBackground; LyricStyle.KARAOKE -> KaraokeBackground; LyricStyle.FADE -> FadeBackground }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title  = { Text("Acerca de LyricMotion") },
            text   = { Text("LyricMotion v1.0.0\nDesarrollado con Jetpack Compose.\n\nVisualiza letras animadas con estilos Neón, Karaoke y Fade.") },
            confirmButton = { TextButton({ showAbout = false }) { Text("Cerrar") } }
        )
    }

    Scaffold(
        containerColor = LMBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = LMPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = LMOnBackground, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LMBackground)
            )
        },
        bottomBar = { navController?.let { LMBottomNav(it) } }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMPrimary), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppLogo(48)
                        Column {
                            Text("LyricMotion", style = MaterialTheme.typography.titleLarge, color = LMOnPrimary, fontWeight = FontWeight.Bold)
                            Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = LMOnPrimary.copy(0.7f))
                        }
                    }
                }
            }

            item { SectionHeader("Visualización") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Estilo predeterminado", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            styles.forEachIndexed { i, label ->
                                val sel = selStyle == i
                                FilterChip(sel, {
                                    selStyle = i
                                    scope.launch { sm.saveDefaultStyle(userEmail, i) }
                                },
                                    label  = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LMPrimary, selectedLabelColor = LMOnPrimary, containerColor = LMSurfaceVariant, labelColor = LMOnSurfaceVariant),
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, selectedBorderColor = LMPrimary, borderColor = LMSurfaceVariant),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(previewBg).padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Look at the stars\nLook how they shine for you",
                                color      = previewColor,
                                fontSize   = fontSize.sp,
                                fontFamily = lyricFontFamily(previewStyle),
                                fontStyle  = if (previewStyle == LyricStyle.FADE) FontStyle.Italic else FontStyle.Normal,
                                textAlign  = TextAlign.Center,
                                lineHeight = (fontSize * 1.7f).sp
                            )
                        }
                    }
                }
            }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TextFields, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tamaño de letra", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text("${fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(fontSize, { fontSize = it; scope.launch { sm.saveFontSize(userEmail, it) } },
                            valueRange = 12f..28f, steps = 7,
                            colors     = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("12sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("28sp", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            item { SectionHeader("Animaciones") }

            item {
                SettCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Velocidad de animación", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Spacer(Modifier.weight(1f))
                            Text(String.format(Locale.getDefault(), "%.1f x", animSpeed), style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(animSpeed, { animSpeed = it; scope.launch { sm.saveAnimationSpeed(userEmail, it) } },
                            valueRange = 0.5f..2f, steps = 5,
                            colors     = SliderDefaults.colors(thumbColor = LMPrimary, activeTrackColor = LMPrimary, inactiveTrackColor = LMSurfaceVariant))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lento",  style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                            Text("Rápido", style = MaterialTheme.typography.labelSmall, color = LMOnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SettCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, null, tint = LMPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Reproducción automática", style = MaterialTheme.typography.titleMedium, color = LMOnBackground)
                            Text("Inicia los efectos al abrir una canción", style = MaterialTheme.typography.bodySmall, color = LMOnSurfaceVariant)
                        }
                        Switch(autoPlay, { autoPlay = it; scope.launch { sm.saveAutoPlay(userEmail, it) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = LMOnPrimary, checkedTrackColor = LMPrimary, uncheckedThumbColor = LMOnSurfaceVariant, uncheckedTrackColor = LMSurfaceVariant))
                    }
                }
            }

            item { SectionHeader("Cuenta") }

            item {
                SettCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = LMError, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Cerrar sesión", style = MaterialTheme.typography.bodyMedium, color = LMError, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        TextButton(onClick = {
                            scope.launch {
                                um.logout()
                                navController?.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                            }
                        }) { Text("Salir", color = LMError, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            item { SectionHeader("General") }

            item {
                SettCard {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Acerca de LyricMotion", style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAbout = true }) {
                                Icon(Icons.Default.ChevronRight, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettRow(Icons.Default.PrivacyTip, "Política de privacidad")
                        HorizontalDivider(color = LMSurfaceVariant, thickness = 0.5.dp)
                        SettRow(Icons.Default.Description, "Términos de uso")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = LMPrimary,
        letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
}

@Composable
private fun SettCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LMSurface), shape = RoundedCornerShape(12.dp)) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SettRow(icon: ImageVector, label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LMOnBackground, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = LMOnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "7 - Settings")
@Composable
fun PreviewSettings() { LyricMotionTheme { SettingsScreen() } }
