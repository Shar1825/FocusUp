package com.example.focusup.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.model.Activity
import com.example.focusup.model.CreateActivityRequest
import com.example.focusup.model.Grade
import com.example.focusup.model.RegisterGradeRequest
import com.example.focusup.model.Rol
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
    val cargando: Boolean = false,
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
                    _uiState.update { it.copy(cargando = false, actividades = lista) }
                }.onFailure {
                    _uiState.update { it.copy(cargando = false, actividades = getActividadesLocalesDefault()) }
                }
            } else {
                val res = studentService.getActivities()
                res.onSuccess { lista ->
                    _uiState.update { it.copy(cargando = false, actividades = lista) }
                }.onFailure {
                    _uiState.update { it.copy(cargando = false, actividades = getActividadesLocalesDefault()) }
                }
            }
        }
    }

    fun crearActividad(req: CreateActivityRequest) {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val res = teacherService.createActivity(req)
            res.onSuccess { nueva ->
                _uiState.update { st ->
                    st.copy(
                        cargando = false,
                        mensaje = "Actividad creada con éxito.",
                        actividades = listOf(nueva) + st.actividades
                    )
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(cargando = false, error = ex.message ?: "No se pudo crear la actividad") }
            }
        }
    }

    fun registrarCalificacion(req: RegisterGradeRequest) {
        viewModelScope.launch {
            val res = teacherService.registerGrade(req)
            res.onSuccess {
                _uiState.update { it.copy(mensaje = "Calificación registrada con éxito.") }
            }.onFailure { ex ->
                _uiState.update { it.copy(error = ex.message) }
            }
        }
    }

    fun setFiltro(filtro: String) {
        _uiState.update { it.copy(filtroEstado = filtro) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }

    private fun getActividadesLocalesDefault(): List<Activity> {
        return listOf(
            Activity(1, 1, 1, "Proyecto Final KMP FocusUp", "Desarrollar el frontend en Kotlin Multiplatform con arquitectura MVVM.", "2024-10-25", "Programación Móvil"),
            Activity(2, 1, 1, "Taller de Cálculo Multivariable", "Resolver la guía de ejercicios prácticos del capítulo 4.", "2024-10-22", "Matemáticas"),
            Activity(3, 2, 2, "Diagrama ER de Base de Datos", "Diseñar las entidades y relaciones para la base de datos PostgreSQL.", "2024-10-28", "Bases de Datos"),
            Activity(4, 2, 2, "Reporte de Calidad de Software", "Evaluar métricas y pruebas unitarias en Kotlin.", "2024-11-02", "Ingeniería de Software")
        )
    }
}
