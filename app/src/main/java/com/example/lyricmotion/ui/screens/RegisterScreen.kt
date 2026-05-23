package com.lyricmotion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lyricmotion.Screen
import com.lyricmotion.User
import com.lyricmotion.UserManager
import com.lyricmotion.ui.components.AppLogo
import com.lyricmotion.ui.theme.LMBackground
import com.lyricmotion.ui.theme.LMError
import com.lyricmotion.ui.theme.LMOnBackground
import com.lyricmotion.ui.theme.LMOnPrimary
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface
import com.lyricmotion.ui.theme.LMSurfaceVariant
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RegisterScreen(navController: NavController? = null, userManager: UserManager? = null) {
    val context  = LocalContext.current
    val um       = userManager ?: remember { UserManager(context) }
    val scope    = rememberCoroutineScope()
    val users    by um.getUsers.collectAsState(initial = emptyList())

    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVis  by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var loading  by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = LMPrimary, unfocusedBorderColor      = LMSurfaceVariant,
        focusedLabelColor       = LMPrimary, unfocusedLabelColor       = LMOnSurfaceVariant,
        cursorColor             = LMPrimary,
        focusedTextColor        = LMOnBackground, unfocusedTextColor   = LMOnBackground,
        focusedContainerColor   = LMSurface, unfocusedContainerColor   = LMSurface
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(LMBackground)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            AppLogo()
            Spacer(Modifier.height(16.dp))
            Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge, color = LMOnBackground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Regístrate para comenzar", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                name, { name = it; errorMsg = "" },
                label       = { Text("NOMBRE") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = LMOnSurfaceVariant) },
                singleLine  = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), colors = tfColors
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                email, { email = it; errorMsg = "" },
                label       = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LMOnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine  = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), colors = tfColors
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                password, { password = it; errorMsg = "" },
                label        = { Text("CONTRASEÑA") },
                leadingIcon  = { Icon(Icons.Default.Lock, null, tint = LMOnSurfaceVariant) },
                trailingIcon = {
                    IconButton({ passVis = !passVis }) {
                        Icon(if (passVis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = LMOnSurfaceVariant)
                    }
                },
                visualTransformation = if (passVis) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine  = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), colors = tfColors
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = LMError)
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) { errorMsg = "Completa todos los campos"; return@Button }
                    if (password.length < 6) { errorMsg = "La contraseña debe tener al menos 6 caracteres"; return@Button }
                    scope.launch {
                        loading = true
                        val ok = um.registerUser(users, User(UUID.randomUUID().toString(), name.trim(), email.trim(), password))
                        loading = false
                        if (ok) navController?.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } }
                        else errorMsg = "El correo ya está registrado"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !loading,
                colors   = ButtonDefaults.buttonColors(containerColor = LMPrimary, contentColor = LMOnPrimary),
                shape    = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(color = LMOnPrimary, modifier = Modifier.size(20.dp))
                else Text("REGISTRARME", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodyMedium, color = LMOnSurfaceVariant)
                TextButton(onClick = { navController?.navigate(Screen.Login.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Inicia sesión", style = MaterialTheme.typography.bodyMedium, color = LMPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
