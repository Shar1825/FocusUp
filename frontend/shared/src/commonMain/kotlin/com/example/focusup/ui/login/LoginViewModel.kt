package com.example.focusup.ui.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.data.ApiAuthRepository
import com.example.focusup.data.AuthRepository
import com.example.focusup.model.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado completo de la pantalla de login y de la sesión.
 * La UI sólo dibuja este objeto; no guarda estado propio.
 *
 * @property usuario null = no hay sesión (se muestra el login); distinto de null = sesión iniciada.
 */
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val verPassword: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val usuario: LoginResponse? = null
)

/**
 * ViewModel del login. Contiene la lógica (validar, llamar al repositorio, manejar
 * resultado) y expone el estado como [StateFlow] (flujo: Repository -> ViewModel -> UI).
 */
class LoginViewModel(
    private val repository: AuthRepository = ApiAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) = _uiState.update { it.copy(correo = valor, error = null) }

    fun onPasswordChange(valor: String) = _uiState.update { it.copy(password = valor, error = null) }

    fun onToggleVerPassword() = _uiState.update { it.copy(verPassword = !it.verPassword) }

    /** Valida los campos y consulta al backend. */
    fun iniciarSesion() {
        val actual = _uiState.value
        if (actual.cargando) return

        if (actual.correo.isBlank() || actual.password.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, complete todos los campos.") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            repository.login(actual.correo.trim(), actual.password)
                .onSuccess { usuario ->
                    // Se limpia la contraseña del estado una vez autenticado
                    _uiState.update { it.copy(cargando = false, password = "", usuario = usuario) }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(cargando = false, error = ex.message ?: "Credenciales o conexión no válidas.")
                    }
                }
        }
    }

    /** Cierra la sesión: vuelve al estado inicial. */
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}