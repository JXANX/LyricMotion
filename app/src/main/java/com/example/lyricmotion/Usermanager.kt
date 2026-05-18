package com.lyricmotion

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "lyricmotion_users")

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String
)

class UserManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        val USERS_KEY        = stringPreferencesKey("users_list")
        val LOGGED_USER_KEY  = stringPreferencesKey("logged_user_email")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")

        // Clave dinámica de canciones guardadas por usuario (email como parte de la clave)
        fun savedSongsKey(email: String) = stringPreferencesKey("saved_songs_$email")
    }

    // ── Usuarios ─────────────────────────────────────────────────

    suspend fun saveUsers(users: List<User>) {
        context.userDataStore.edit { it[USERS_KEY] = gson.toJson(users) }
    }

    val getUsers: Flow<List<User>> = context.userDataStore.data.map { prefs ->
        val json = prefs[USERS_KEY] ?: ""
        if (json.isEmpty()) emptyList()
        else {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(json, type)
        }
    }

    suspend fun registerUser(currentUsers: List<User>, newUser: User): Boolean {
        if (currentUsers.any { it.email == newUser.email }) return false
        saveUsers(currentUsers + newUser)
        return true
    }

    suspend fun loginUser(users: List<User>, email: String, password: String): User? {
        val user = users.find { it.email == email && it.password == password }
        if (user != null) {
            context.userDataStore.edit { prefs ->
                prefs[LOGGED_USER_KEY]  = email
                prefs[IS_LOGGED_IN_KEY] = true
            }
        }
        return user
    }

    suspend fun logout() {
        context.userDataStore.edit { prefs ->
            prefs[LOGGED_USER_KEY]  = ""
            prefs[IS_LOGGED_IN_KEY] = false
        }
    }

    val isLoggedIn: Flow<Boolean> = context.userDataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    val loggedUserEmail: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[LOGGED_USER_KEY] ?: ""
    }

    // ── Canciones Guardadas (por usuario) ────────────────────────

    fun getSavedSongIds(email: String): Flow<List<String>> =
        context.userDataStore.data.map { prefs ->
            val json = prefs[savedSongsKey(email)] ?: ""
            if (json.isEmpty()) emptyList()
            else {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type)
            }
        }

    suspend fun saveSongForUser(email: String, songId: String) {
        context.userDataStore.edit { prefs ->
            val key  = savedSongsKey(email)
            val json = prefs[key] ?: ""
            val current: MutableList<String> = if (json.isEmpty()) mutableListOf()
            else {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(json, type).toMutableList()
            }
            if (!current.contains(songId)) {
                current.add(songId)
                prefs[key] = gson.toJson(current)
            }
        }
    }

    suspend fun removeSongForUser(email: String, songId: String) {
        context.userDataStore.edit { prefs ->
            val key  = savedSongsKey(email)
            val json = prefs[key] ?: ""
            if (json.isEmpty()) return@edit
            val type = object : TypeToken<List<String>>() {}.type
            val current: MutableList<String> = gson.fromJson<List<String>>(json, type).toMutableList()
            current.remove(songId)
            prefs[key] = gson.toJson(current)
        }
    }

    // Obtener email del usuario actual de forma suspendida (para Workers)
    suspend fun getCurrentEmail(): String =
        context.userDataStore.data.map { it[LOGGED_USER_KEY] ?: "" }.first()
}