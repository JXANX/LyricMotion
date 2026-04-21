package com.example.lyricmotion

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── Instancia única de DataStore para usuarios ───────────────────
val Context.userDataStore by preferencesDataStore(name = "lyricmotion_users")

/**
 * Modelo de datos del usuario.
 * Se serializa a JSON con Gson para guardarlo en DataStore.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String   // En producción real se usaría hash, aquí es prototipo
)

/**
 * UserManager — única clase responsable del almacenamiento de usuarios y sesión.
 *
 * Persiste:
 *  - Lista de usuarios registrados (JSON)
 *  - Usuario actualmente logueado (email)
 *  - Estado de sesión activa (booleano)
 */
class UserManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        val USERS_KEY          = stringPreferencesKey("users_list")
        val LOGGED_USER_KEY    = stringPreferencesKey("logged_user_email")
        val IS_LOGGED_IN_KEY   = booleanPreferencesKey("is_logged_in")
    }

    // ── Guardar lista de usuarios ────────────────────────────────
    suspend fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        context.userDataStore.edit { prefs ->
            prefs[USERS_KEY] = json
        }
    }

    // ── Leer lista de usuarios (Flow reactivo) ───────────────────
    val getUsers: Flow<List<User>> = context.userDataStore.data.map { prefs ->
        val json = prefs[USERS_KEY] ?: ""
        if (json.isEmpty()) emptyList()
        else {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(json, type)
        }
    }

    // ── Registrar un nuevo usuario ───────────────────────────────
    suspend fun registerUser(currentUsers: List<User>, newUser: User): Boolean {
        // Verificar que el email no exista ya
        if (currentUsers.any { it.email == newUser.email }) return false
        saveUsers(currentUsers + newUser)
        return true
    }

    // ── Iniciar sesión ───────────────────────────────────────────
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

    // ── Cerrar sesión ────────────────────────────────────────────
    suspend fun logout() {
        context.userDataStore.edit { prefs ->
            prefs[LOGGED_USER_KEY]  = ""
            prefs[IS_LOGGED_IN_KEY] = false
        }
    }

    // ── ¿Hay sesión activa? (Flow reactivo) ─────────────────────
    val isLoggedIn: Flow<Boolean> = context.userDataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    // ── Email del usuario logueado (Flow reactivo) ───────────────
    val loggedUserEmail: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[LOGGED_USER_KEY] ?: ""
    }
}