package com.example.focusup.ui.estudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.model.EvidenciaResumen
import com.example.focusup.model.SesionRequest
import com.example.focusup.network.EstudioService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Estados explícitos del ciclo de vida de una sesión de estudio en FocusUp.
 */
enum class EstadoSesionEstudio {
    CONFIGURACION,          // Definiendo objetivo y duración
    EN_CURSO,               // Temporizador activo corriendo
    PAUSADO,                // Temporizador pausado por el estudiante
    ESPERANDO_FOTO,         // Temporizador en 0, esperando evidencia fotográfica
    SUBIENDO_EVIDENCIA,     // Enviando multipart al servidor (POST /api/evidencias)
    PENDIENTE_VALIDACION,   // Esperando revisión del docente (polling GET /api/evidencias/{id})
    APROBADA,               // Docente aprobó: sesión finalizada y dispositivo liberado
    RECHAZADA               // Docente rechazó: muestra motivo y permite reenviar foto
}

data class StudyUiState(
    val estudianteId: Int = 1,
    val objetivo: String = "",
    val duracionMinutos: Int = 25,
    val segundosRestantes: Int = 25 * 60,
    val estadoSesion: EstadoSesionEstudio = EstadoSesionEstudio.CONFIGURACION,
    val sesionIdActual: Int? = null,
    val evidenciaIdActual: Int? = null,
    val comentarioDocente: String? = null,
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val evidenciasPendientes: List<EvidenciaResumen> = emptyList()
)

class StudyViewModel(
    private val estudioService: EstudioService = EstudioService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var pollingJob: Job? = null

    fun setObjetivo(nuevoObjetivo: String) {
        _uiState.update {
            it.copy(
                objetivo = nuevoObjetivo,
                error = if (nuevoObjetivo.isNotBlank() && it.error?.contains("objetivo", ignoreCase = true) == true) null else it.error
            )
        }
    }

    fun setDuracion(minutos: Int) {
        if (_uiState.value.estadoSesion == EstadoSesionEstudio.CONFIGURACION) {
            _uiState.update {
                it.copy(
                    duracionMinutos = minutos,
                    segundosRestantes = minutos * 60,
                    error = if (minutos > 0 && it.error?.contains("duración", ignoreCase = true) == true) null else it.error
                )
            }
        }
    }

    /**
     * Punto 3: Valida datos, envía POST /api/sesiones y sólo al recibir éxito inicia el temporizador.
     */
    fun iniciarSesionPomodoro(estudianteId: Int) {
        val actual = _uiState.value

        val errorValidacion = when {
            actual.estadoSesion != EstadoSesionEstudio.CONFIGURACION -> "Ya existe una sesión en curso o en proceso de validación."
            actual.objetivo.isBlank() -> "El objetivo de estudio no puede estar vacío."
            actual.duracionMinutos <= 0 -> "La duración de la sesión debe ser mayor a 0 minutos."
            else -> null
        }

        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }

        _uiState.update {
            it.copy(estudianteId = estudianteId, cargando = true, error = null, mensaje = null)
        }

        viewModelScope.launch {
            val req = SesionRequest(estudianteId, actual.objetivo.trim(), actual.duracionMinutos)
            estudioService.crearSesion(req)
                .onSuccess { res ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            sesionIdActual = res.id,
                            estadoSesion = EstadoSesionEstudio.EN_CURSO,
                            segundosRestantes = it.duracionMinutos * 60,
                            mensaje = "¡Sesión #${res.id} registrada en el servidor! Iniciando tiempo de concentración."
                        )
                    }
                    iniciarTimerLoop()
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "No se pudo iniciar la sesión en el servidor: ${ex.message}. Verifica la conexión con el backend."
                        )
                    }
                }
        }
    }

    /**
     * Temporizador regresivo. Al llegar a cero cambia a ESPERANDO_FOTO.
     */
    private fun iniciarTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.segundosRestantes > 0 && _uiState.value.estadoSesion == EstadoSesionEstudio.EN_CURSO) {
                delay(1000)
                if (_uiState.value.estadoSesion == EstadoSesionEstudio.EN_CURSO) {
                    _uiState.update { current ->
                        val nuevo = current.segundosRestantes - 1
                        if (nuevo <= 0) {
                            current.copy(
                                segundosRestantes = 0,
                                estadoSesion = EstadoSesionEstudio.ESPERANDO_FOTO,
                                mensaje = "¡Tiempo concluido! Captura y envía la evidencia fotográfica de tu sesión."
                            )
                        } else {
                            current.copy(segundosRestantes = nuevo)
                        }
                    }
                }
            }
        }
    }

    fun pausarTimer() {
        if (_uiState.value.estadoSesion == EstadoSesionEstudio.EN_CURSO) {
            timerJob?.cancel()
            _uiState.update { it.copy(estadoSesion = EstadoSesionEstudio.PAUSADO) }
        }
    }

    fun reanudarTimer() {
        if (_uiState.value.estadoSesion == EstadoSesionEstudio.PAUSADO) {
            _uiState.update { it.copy(estadoSesion = EstadoSesionEstudio.EN_CURSO) }
            iniciarTimerLoop()
        }
    }

    fun cancelarSesion() = resetearSesion(mensaje = "Sesión cancelada.")

    fun iniciarNuevaSesion() = resetearSesion(mensaje = null)

    private fun resetearSesion(mensaje: String?) {
        timerJob?.cancel()
        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                estadoSesion = EstadoSesionEstudio.CONFIGURACION,
                segundosRestantes = it.duracionMinutos * 60,
                sesionIdActual = null,
                evidenciaIdActual = null,
                comentarioDocente = null,
                cargando = false,
                mensaje = mensaje,
                error = null
            )
        }
    }

    fun simularFinDeTiempoParaPruebas() {
        if (_uiState.value.estadoSesion == EstadoSesionEstudio.EN_CURSO || _uiState.value.estadoSesion == EstadoSesionEstudio.PAUSADO) {
            _uiState.update { it.copy(segundosRestantes = 3) }
        }
    }

    // =========================================================================
    // PUNTO 4: Integración con Cámara y Evidencias
    // =========================================================================

    fun subirEvidencia(imagenBytes: ByteArray) {
        val sid = _uiState.value.sesionIdActual
        if (sid == null) {
            _uiState.update { it.copy(error = "No hay una sesión de estudio activa asociada para enviar la evidencia.") }
            return
        }

        _uiState.update {
            it.copy(
                cargando = true,
                estadoSesion = EstadoSesionEstudio.SUBIENDO_EVIDENCIA,
                error = null,
                mensaje = "Enviando fotografía al servidor..."
            )
        }

        viewModelScope.launch {
            estudioService.subirEvidencia(sid, imagenBytes)
                .onSuccess { evidencia ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            evidenciaIdActual = evidencia.id,
                            estadoSesion = EstadoSesionEstudio.PENDIENTE_VALIDACION,
                            mensaje = "Tu evidencia fue enviada correctamente. Esperando validación del docente."
                        )
                    }
                    iniciarConsultaPeriodica(evidencia.id)
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            estadoSesion = EstadoSesionEstudio.ESPERANDO_FOTO,
                            error = "Error al enviar evidencia fotográfica: ${ex.message}. Intenta nuevamente."
                        )
                    }
                }
        }
    }

    fun onEvidenciaEnviadaExternamente(evidenciaId: Int) {
        _uiState.update {
            it.copy(
                evidenciaIdActual = evidenciaId,
                estadoSesion = EstadoSesionEstudio.PENDIENTE_VALIDACION,
                mensaje = "Tu evidencia fue enviada correctamente. Esperando validación del docente.",
                error = null
            )
        }
        iniciarConsultaPeriodica(evidenciaId)
    }

    private fun iniciarConsultaPeriodica(evidenciaId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                estudioService.consultarEstadoEvidencia(evidenciaId).onSuccess { estadoEvidencia ->
                    when (estadoEvidencia.estado.uppercase()) {
                        "APROBADA" -> {
                            _uiState.update {
                                it.copy(
                                    estadoSesion = EstadoSesionEstudio.APROBADA,
                                    comentarioDocente = estadoEvidencia.comentario,
                                    mensaje = "¡Evidencia aprobada! Tu sesión de estudio ha finalizado y el dispositivo quedó liberado."
                                )
                            }
                            return@launch
                        }
                        "RECHAZADA" -> {
                            _uiState.update {
                                it.copy(
                                    estadoSesion = EstadoSesionEstudio.RECHAZADA,
                                    comentarioDocente = estadoEvidencia.comentario
                                        ?: "Evidencia rechazada por el docente. Se requiere una nueva fotografía.",
                                    mensaje = "La evidencia fue rechazada por el docente. Por favor captura una nueva fotografía."
                                )
                            }
                            return@launch
                        }
                    }
                }
            }
        }
    }

    fun reintentarCapturaFoto() {
        _uiState.update {
            it.copy(
                estadoSesion = EstadoSesionEstudio.ESPERANDO_FOTO,
                mensaje = "Toma una nueva fotografía clara de tu evidencia de estudio.",
                error = null
            )
        }
    }

    // =========================================================================
    // Funcionalidades para Docente (Validación y listado de evidencias)
    // =========================================================================

    fun cargarEvidenciasPendientesDocente() {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            estudioService.getEvidenciasPendientes()
                .onSuccess { lista -> _uiState.update { it.copy(cargando = false, evidenciasPendientes = lista) } }
                .onFailure { _uiState.update { it.copy(cargando = false) } }
        }
    }

    fun validarEvidencia(id: Int, estado: String, comentario: String? = null) {
        viewModelScope.launch {
            estudioService.validarEvidencia(id, estado, comentario)
                .onSuccess {
                    _uiState.update { st ->
                        st.copy(
                            mensaje = "Evidencia #$id marcada como $estado.",
                            evidenciasPendientes = st.evidenciasPendientes.filter { it.id != id }
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(error = "No se pudo validar evidencia: ${ex.message}") }
                }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        pollingJob?.cancel()
    }
}
