package com.example.lyricmotion

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── Instancia única de DataStore para ajustes ────────────────────
val Context.settingsDataStore by preferencesDataStore(name = "lyricmotion_settings")

/**
 * Modelo de datos de las preferencias del usuario.
 * Se usa para pasar el estado completo a la UI de una vez.
 */
data class AppSettings(
    val defaultStyleIndex: Int     = 0,     // 0=Fade, 1=Neon, 2=Karaoke
    val fontSize:          Float   = 16f,   // sp
    val animationSpeed:    Float   = 1f,    // multiplicador
    val autoPlay:          Boolean = true
)

/**
 * SettingsManager — única clase responsable de las preferencias de ajustes.
 *
 * Persiste:
 *  - Estilo de visualización predeterminado
 *  - Tamaño de fuente
 *  - Velocidad de animación
 *  - Reproducción automática
 */
class SettingsManager(private val context: Context) {

    companion object {
        val DEFAULT_STYLE_KEY   = intPreferencesKey("default_style")
        val FONT_SIZE_KEY       = floatPreferencesKey("font_size")
        val ANIMATION_SPEED_KEY = floatPreferencesKey("animation_speed")
        val AUTO_PLAY_KEY       = booleanPreferencesKey("auto_play")
    }

    // ── Leer ajustes completos (Flow reactivo) ───────────────────
    val getSettings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            defaultStyleIndex = prefs[DEFAULT_STYLE_KEY]   ?: 0,
            fontSize          = prefs[FONT_SIZE_KEY]       ?: 16f,
            animationSpeed    = prefs[ANIMATION_SPEED_KEY] ?: 1f,
            autoPlay          = prefs[AUTO_PLAY_KEY]       ?: true
        )
    }

    // ── Guardar estilo predeterminado ────────────────────────────
    suspend fun saveDefaultStyle(index: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[DEFAULT_STYLE_KEY] = index
        }
    }

    // ── Guardar tamaño de fuente ─────────────────────────────────
    suspend fun saveFontSize(size: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = size
        }
    }

    // ── Guardar velocidad de animación ───────────────────────────
    suspend fun saveAnimationSpeed(speed: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[ANIMATION_SPEED_KEY] = speed
        }
    }

    // ── Guardar autoplay ─────────────────────────────────────────
    suspend fun saveAutoPlay(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[AUTO_PLAY_KEY] = enabled
        }
    }

    // ── Guardar todos los ajustes de una vez ─────────────────────
    suspend fun saveAllSettings(settings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[DEFAULT_STYLE_KEY]   = settings.defaultStyleIndex
            prefs[FONT_SIZE_KEY]       = settings.fontSize
            prefs[ANIMATION_SPEED_KEY] = settings.animationSpeed
            prefs[AUTO_PLAY_KEY]       = settings.autoPlay
        }
    }
}
