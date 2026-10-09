package com.example.focusup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusup.ui.login.LoginScreen
import com.example.focusup.ui.login.LoginViewModel
import com.example.focusup.ui.main.MainScreen
import com.example.focusup.ui.theme.FocusUpTheme

/**
 * Punto de entrada de la UI compartida (Android / iOS / Desktop).
 * Sólo decide qué pantalla mostrar: sin sesión -> [LoginScreen]; con sesión -> [MainScreen]
 * (que a su vez enruta al panel según el rol). La lógica vive en [LoginViewModel].
 */
@Composable
fun App() {
    FocusUpTheme {
        val viewModel = viewModel { LoginViewModel() }
        val state by viewModel.uiState.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // safeDrawingPadding: evita que el contenido quede bajo la barra de estado/navegación
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                Crossfade(targetState = state.usuario, label = "navegacion") { usuario ->
                    if (usuario == null) {
                        LoginScreen(
                            state = state,
                            onCorreoChange = viewModel::onCorreoChange,
                            onPasswordChange = viewModel::onPasswordChange,
                            onToggleVerPassword = viewModel::onToggleVerPassword,
                            onIniciarSesion = viewModel::iniciarSesion
                        )
                    } else {
                        MainScreen(usuario = usuario, onCerrarSesion = viewModel::cerrarSesion)
                    }
                }
            }
        }
    }
}