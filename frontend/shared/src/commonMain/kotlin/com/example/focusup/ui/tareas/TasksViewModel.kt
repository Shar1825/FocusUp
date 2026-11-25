package com.example.focusup.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.model.Activity
import com.example.focusup.model.CreateActivityRequest
import com.example.focusup.model.Grade
import com.example.focusup.model.Group
import com.example.focusup.model.RegisterGradeRequest
import com.example.focusup.model.Rol
import com.example.focusup.model.UserDto
import com.example.focusup.network.StudentService
import com.example.focusup.network.TeacherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TasksUiState(
    val actividades: List<Activity> = emptyList(),
    val calificaciones: List<Grade> = emptyList(),
    val grupos: List<Group> = emptyList(),
    val estudiantes: List<UserDto> = emptyList(),
    val cargando: Boolean = false,
    val cargandoModal: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val filtroEstado: String = "Todas"
)

class TasksViewModel(
    private val studentService: StudentService = StudentService(),
    private val teacherService: TeacherService = TeacherService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    fun cargarActividades(rol: Rol) {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            if (rol == Rol.DOCENTE) {
                val res = teacherService.getActivities()
                res.onSuccess { lista ->
                    _uiState.update { it.copy(cargando = false, actividades = lista, error = null) }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            actividades = emptyList(),
                            error = "Sin conexión con el servidor: ${ex.message}. Verifica la conexión con la API de FocusUp."
                        )
                    }
                }
            } else {
                val res = studentService.getActivities()
                res.onSuccess { lista ->
                    _uiState.update { it.copy(cargando = false, actividades = lista, error = null) }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            actividades = emptyList(),
                            error = "Sin conexión con el servidor: ${ex.message}. Verifica la conexión con la API de FocusUp."
                        )
                    }
                }
            }
        }
    }

    fun cargarGruposDocente() {
        _uiState.update { it.copy(cargandoModal = true, error = null) }
        viewModelScope.launch {
            teacherService.getGroups()
                .onSuccess { lista ->
                    _uiState.update { it.copy(cargandoModal = false, grupos = lista) }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargandoModal = false,
                            error = "No se pudieron cargar los grupos del docente: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun cargarEstudiantesDocente() {
        _uiState.update { it.copy(cargandoModal = true, error = null) }
        viewModelScope.launch {
            teacherService.getStudents()
                .onSuccess { lista ->
                    _uiState.update { it.copy(cargandoModal = false, estudiantes = lista) }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            cargandoModal = false,
                            error = "No se pudieron cargar los estudiantes: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun crearActividad(req: CreateActivityRequest, rolActual: Rol) {
        if (req.titulo.isBlank()) {
            _uiState.update { it.copy(error = "El título de la actividad no puede estar vacío.") }
            return
        }
        if (req.fechaEntrega.isBlank()) {
            _uiState.update { it.copy(error = "La fecha de entrega no puede estar vacía.") }
            return
        }
        if (req.grupoId <= 0) {
            _uiState.update { it.copy(error = "Debes seleccionar un grupo válido.") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val res = teacherService.createActivity(req)
            res.onSuccess {
                _uiState.update { st ->
                    st.copy(
                        cargando = false,
                        mensaje = "Actividad creada con éxito."
                    )
                }
                cargarActividades(rolActual)
            }.onFailure { ex ->
                _uiState.update { it.copy(cargando = false, error = "Error al crear actividad: ${ex.message}") }
            }
        }
    }

    fun registrarCalificacion(req: RegisterGradeRequest) {
        if (req.actividadId <= 0) {
            _uiState.update { it.copy(error = "ID de actividad inválido.") }
            return
        }
        if (req.estudianteId <= 0) {
            _uiState.update { it.copy(error = "Debes seleccionar un estudiante para calificar.") }
            return
        }
        if (req.calificacion < 0.0 || req.calificacion > 10.0) {
            _uiState.update { it.copy(error = "La calificación debe ser un valor numérico entre 0.0 y 10.0.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargandoModal = true, error = null) }
            val res = teacherService.registerGrade(req)
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        cargandoModal = false,
                        mensaje = "Calificación registrada con éxito."
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        cargandoModal = false,
                        error = "Error al registrar calificación: ${ex.message}"
                    )
                }
            }
        }
    }

    fun setFiltro(filtro: String) {
        _uiState.update { it.copy(filtroEstado = filtro) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }
}
