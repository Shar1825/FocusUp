package com.example.focusup.ui.estudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.data.EstadoSesionGuardada
import com.example.focusup.data.StudyRepository
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
    val ultimaImagenBytes: ByteArray? = null,
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
     * Restaura el estado de la sesión del estudiante si existe una activa en el repositorio compartido.
     */
    fun restaurarSesionEstudiante(estudianteId: Int) {
        _uiState.update { it.copy(estudianteId = estudianteId) }
        val guardada = StudyRepository.obtenerSesionEstudiante(estudianteId)
        if (guardada != null && guardada.estadoSesion != EstadoSesionEstudio.CONFIGURACION) {
            _uiState.update {
                it.copy(
                    estudianteId = estudianteId,
                    sesionIdActual = guardada.sesionId,
                    evidenciaIdActual = guardada.evidenciaId,
                    objetivo = guardada.objetivo,
                    duracionMinutos = guardada.duracionMinutos,
                    segundosRestantes = guardada.segundosRestantes,
                    estadoSesion = guardada.estadoSesion,
                    ultimaImagenBytes = guardada.ultimaImagenBytes ?: it.ultimaImagenBytes,
                    comentarioDocente = guardada.comentarioDocente
                )
            }
            if (guardada.evidenciaId != null && (guardada.estadoSesion == EstadoSesionEstudio.PENDIENTE_VALIDACION || guardada.estadoSesion == EstadoSesionEstudio.SUBIENDO_EVIDENCIA)) {
                iniciarConsultaPeriodica(guardada.evidenciaId)
            }
        }
    }

    /**
     * Valida datos, genera sesión local e inicia el temporizador de inmediato.
     * También intenta sincronizar la creación con el servidor Ktor.
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

        val localSesionId = (100..999).random()

        _uiState.update {
            it.copy(
                estudianteId = estudianteId,
                sesionIdActual = localSesionId,
                estadoSesion = EstadoSesionEstudio.EN_CURSO,
                segundosRestantes = actual.duracionMinutos * 60,
                cargando = false,
                error = null,
                mensaje = "¡Sesión de estudio iniciada! Tiempo de concentración activo."
            )
        }

        StudyRepository.guardarSesionEstudiante(
            EstadoSesionGuardada(
                estudianteId = estudianteId,
                sesionId = localSesionId,
                evidenciaId = null,
                objetivo = actual.objetivo.trim(),
                duracionMinutos = actual.duracionMinutos,
                segundosRestantes = actual.duracionMinutos * 60,
                estadoSesion = EstadoSesionEstudio.EN_CURSO,
                ultimaImagenBytes = null,
                comentarioDocente = null
            )
        )

        iniciarTimerLoop()

        viewModelScope.launch {
            val req = SesionRequest(estudianteId, actual.objetivo.trim(), actual.duracionMinutos)
            estudioService.crearSesion(req).onSuccess { res ->
                _uiState.update { it.copy(sesionIdActual = res.id) }
                StudyRepository.guardarSesionEstudiante(
                    EstadoSesionGuardada(
                        estudianteId = estudianteId,
                        sesionId = res.id,
                        evidenciaId = null,
                        objetivo = actual.objetivo.trim(),
                        duracionMinutos = actual.duracionMinutos,
                        segundosRestantes = _uiState.value.segundosRestantes,
                        estadoSesion = _uiState.value.estadoSesion,
                        ultimaImagenBytes = _uiState.value.ultimaImagenBytes,
                        comentarioDocente = null
                    )
                )
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
                        val estadoNuevo = if (nuevo <= 0) EstadoSesionEstudio.ESPERANDO_FOTO else current.estadoSesion
                        val msg = if (nuevo <= 0) "¡Tiempo concluido! Captura y envía la evidencia fotográfica de tu sesión." else current.mensaje

                        val st = current.copy(
                            segundosRestantes = nuevo.coerceAtLeast(0),
                            estadoSesion = estadoNuevo,
                            mensaje = msg
                        )

                        current.sesionIdActual?.let { sid ->
                            StudyRepository.guardarSesionEstudiante(
                                EstadoSesionGuardada(
                                    estudianteId = current.estudianteId,
                                    sesionId = sid,
                                    evidenciaId = current.evidenciaIdActual,
                                    objetivo = current.objetivo,
                                    duracionMinutos = current.duracionMinutos,
                                    segundosRestantes = st.segundosRestantes,
                                    estadoSesion = estadoNuevo,
                                    ultimaImagenBytes = current.ultimaImagenBytes,
                                    comentarioDocente = current.comentarioDocente
                                )
                            )
                        }

                        st
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
        val sid = _uiState.value.estudianteId
        _uiState.update {
            it.copy(
                estadoSesion = EstadoSesionEstudio.CONFIGURACION,
                segundosRestantes = it.duracionMinutos * 60,
                sesionIdActual = null,
                evidenciaIdActual = null,
                comentarioDocente = null,
                ultimaImagenBytes = null,
                cargando = false,
                mensaje = mensaje,
                error = null
            )
        }
        StudyRepository.guardarSesionEstudiante(
            EstadoSesionGuardada(
                estudianteId = sid,
                sesionId = 0,
                evidenciaId = null,
                objetivo = "",
                duracionMinutos = 25,
                segundosRestantes = 25 * 60,
                estadoSesion = EstadoSesionEstudio.CONFIGURACION,
                ultimaImagenBytes = null,
                comentarioDocente = null
            )
        )
    }

    fun simularFinDeTiempoParaPruebas() {
        if (_uiState.value.estadoSesion == EstadoSesionEstudio.EN_CURSO || _uiState.value.estadoSesion == EstadoSesionEstudio.PAUSADO) {
            _uiState.update { it.copy(segundosRestantes = 3) }
        }
    }

    // =========================================================================
    // Integración con Cámara y Evidencias
    // =========================================================================

    fun subirEvidencia(imagenBytes: ByteArray) {
        val sid = _uiState.value.sesionIdActual ?: (100..999).random()
        val evidenciaIdLocal = _uiState.value.evidenciaIdActual ?: (1000..9999).random()

        val evNueva = EvidenciaResumen(
            id = evidenciaIdLocal,
            sesionId = sid,
            estudiante = "Estudiante FocusUp",
            objetivo = _uiState.value.objetivo.ifBlank { "Estudio Concentrado" },
            estado = "PENDIENTE",
            imagenBytes = imagenBytes
        )

        // Registrar inmediatamente en StudyRepository de forma persistente en memoria
        StudyRepository.agregarEvidenciaPendiente(evNueva)

        StudyRepository.guardarSesionEstudiante(
            EstadoSesionGuardada(
                estudianteId = _uiState.value.estudianteId,
                sesionId = sid,
                evidenciaId = evidenciaIdLocal,
                objetivo = _uiState.value.objetivo,
                duracionMinutos = _uiState.value.duracionMinutos,
                segundosRestantes = 0,
                estadoSesion = EstadoSesionEstudio.PENDIENTE_VALIDACION,
                ultimaImagenBytes = imagenBytes,
                comentarioDocente = null
            )
        )

        _uiState.update { current ->
            current.copy(
                cargando = false,
                sesionIdActual = sid,
                evidenciaIdActual = evidenciaIdLocal,
                estadoSesion = EstadoSesionEstudio.PENDIENTE_VALIDACION,
                ultimaImagenBytes = imagenBytes,
                mensaje = "Tu evidencia fue enviada correctamente. Esperando validación del docente.",
                error = null,
                evidenciasPendientes = listOf(evNueva) + current.evidenciasPendientes.filter { it.id != evidenciaIdLocal }
            )
        }

        iniciarConsultaPeriodica(evidenciaIdLocal)

        // Enviar al backend Ktor en segundo plano
        viewModelScope.launch {
            estudioService.subirEvidencia(sid, imagenBytes)
                .onSuccess { evidenciaRed ->
                    if (evidenciaRed.id != evidenciaIdLocal) {
                        val evActualizada = evNueva.copy(id = evidenciaRed.id)
                        StudyRepository.removerEvidenciaPendiente(evidenciaIdLocal)
                        StudyRepository.agregarEvidenciaPendiente(evActualizada)
                        _uiState.update { st ->
                            st.copy(
                                evidenciaIdActual = evidenciaRed.id,
                                evidenciasPendientes = listOf(evActualizada) + st.evidenciasPendientes.filter { it.id != evidenciaIdLocal }
                            )
                        }
                    }
                }
        }
    }

    private fun iniciarConsultaPeriodica(evidenciaId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(2000)

                // 1. Revisar estado local actualizado por el Docente en la app
                val local = StudyRepository.obtenerSesionEstudiante(_uiState.value.estudianteId)
                if (local != null && local.evidenciaId == evidenciaId) {
                    if (local.estadoSesion == EstadoSesionEstudio.APROBADA) {
                        _uiState.update {
                            it.copy(
                                estadoSesion = EstadoSesionEstudio.APROBADA,
                                comentarioDocente = local.comentarioDocente,
                                mensaje = "¡Evidencia aprobada! Tu sesión de estudio ha finalizado y el dispositivo quedó liberado."
                            )
                        }
                        return@launch
                    } else if (local.estadoSesion == EstadoSesionEstudio.RECHAZADA) {
                        _uiState.update {
                            it.copy(
                                estadoSesion = EstadoSesionEstudio.RECHAZADA,
                                comentarioDocente = local.comentarioDocente
                                    ?: "Evidencia rechazada por el docente. Se requiere una nueva fotografía.",
                                mensaje = "La evidencia fue rechazada por el docente. Por favor captura una nueva fotografía."
                            )
                        }
                        return@launch
                    }
                }

                // 2. Consultar al servidor Ktor
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

        // 1. Obtener pendientes en repositorio local como respaldo rápido
        val pendientesDirectas = StudyRepository.evidenciasPendientes.value.filter { it.estado == "PENDIENTE" }
        val evidenciasDeSesiones = StudyRepository.sesionesEstudiantes.value.values
            .filter { it.estadoSesion == EstadoSesionEstudio.PENDIENTE_VALIDACION && it.ultimaImagenBytes != null }
            .map { s ->
                EvidenciaResumen(
                    id = s.evidenciaId ?: s.sesionId,
                    sesionId = s.sesionId,
                    estudiante = "Estudiante FocusUp",
                    objetivo = s.objetivo.ifBlank { "Estudio Concentrado" },
                    estado = "PENDIENTE",
                    imagenBytes = s.ultimaImagenBytes
                )
            }

        val locales = (pendientesDirectas + evidenciasDeSesiones).distinctBy { it.id }

        // Muestra de inmediato lo que haya local para no dejar la pantalla en blanco
        _uiState.update { it.copy(evidenciasPendientes = locales) }

        // 2. Traer directamente de la base de datos MySQL en Railway a través de Ktor
        viewModelScope.launch {
            estudioService.getEvidenciasPendientes()
                .onSuccess { red ->
                    // Filtrar las evidencias que están en PENDIENTE en el backend
                    val soloPendientesRed = red.filter {
                        it.estado.equals("PENDIENTE", ignoreCase = true) ||
                                it.estado.equals("PENDIENTE_VALIDACION", ignoreCase = true)
                    }

                    // Combinar las imágenes de los bytes locales si la red no devolvió la imagen binaria
                    val combinadas = soloPendientesRed.map { ev ->
                        val localEv = locales.find { it.id == ev.id || it.sesionId == ev.sesionId }
                        if (ev.imagenBytes == null && localEv?.imagenBytes != null) {
                            ev.copy(imagenBytes = localEv.imagenBytes)
                        } else {
                            ev
                        }
                    }

                    val resultadoFinal = if (combinadas.isNotEmpty()) combinadas else locales

                    _uiState.update {
                        it.copy(
                            cargando = false,
                            evidenciasPendientes = resultadoFinal,
                            error = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            evidenciasPendientes = locales,
                            error = "No se pudieron obtener las evidencias del servidor Ktor: ${err.message}"
                        )
                    }
                }
        }
    }

    fun validarEvidencia(id: Int, estado: String, comentario: String? = null) {
        viewModelScope.launch {
            StudyRepository.actualizarEstadoEvidencia(id, estado, comentario)
            StudyRepository.removerEvidenciaPendiente(id)

            _uiState.update { st ->
                st.copy(
                    mensaje = "Evidencia #$id marcada como $estado.",
                    evidenciasPendientes = st.evidenciasPendientes.filter { it.id != id }
                )
            }

            estudioService.validarEvidencia(id, estado, comentario)
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
