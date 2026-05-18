package com.lyricmotion

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "lyricmotion_settings")

data class AppSettings(
    val defaultStyleIndex: Int     = 0,
    val fontSize:          Float   = 16f,
    val animationSpeed:    Float   = 1f,
    val autoPlay:          Boolean = true
)

class SettingsManager(private val context: Context) {

    companion object {
        fun styleKey(email: String)     = intPreferencesKey("default_style_$email")
        fun fontSizeKey(email: String)  = floatPreferencesKey("font_size_$email")
        fun animSpeedKey(email: String) = floatPreferencesKey("animation_speed_$email")
        fun autoPlayKey(email: String)  = booleanPreferencesKey("auto_play_$email")
    }

    fun getSettingsForUser(email: String): Flow<AppSettings> =
        context.settingsDataStore.data.map { prefs ->
            AppSettings(
                defaultStyleIndex = prefs[styleKey(email)]     ?: 0,
                fontSize          = prefs[fontSizeKey(email)]  ?: 16f,
                animationSpeed    = prefs[animSpeedKey(email)] ?: 1f,
                autoPlay          = prefs[autoPlayKey(email)]  ?: true
            )
        }

    suspend fun saveDefaultStyle(email: String, index: Int) {
        context.settingsDataStore.edit { it[styleKey(email)] = index }
    }

    suspend fun saveFontSize(email: String, size: Float) {
        context.settingsDataStore.edit { it[fontSizeKey(email)] = size }
    }

    suspend fun saveAnimationSpeed(email: String, speed: Float) {
        context.settingsDataStore.edit { it[animSpeedKey(email)] = speed }
    }

    suspend fun saveAutoPlay(email: String, enabled: Boolean) {
        context.settingsDataStore.edit { it[autoPlayKey(email)] = enabled }
    }
}