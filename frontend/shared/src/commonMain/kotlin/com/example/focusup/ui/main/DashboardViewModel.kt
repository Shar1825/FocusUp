package com.example.focusup.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusup.model.AcademicProgress
import com.example.focusup.model.Activity
import com.example.focusup.model.AdminStatistics
import com.example.focusup.model.Rol
import com.example.focusup.model.TeacherProfile
import com.example.focusup.network.AdminService
import com.example.focusup.network.StudentService
import com.example.focusup.network.TeacherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val studentProgress: AcademicProgress? = null,
    val teacherProfile: TeacherProfile? = null,
    val adminStatistics: AdminStatistics? = null,
    val actividadesEstudiante: List<Activity> = emptyList()
)

class DashboardViewModel(
    private val studentService: StudentService = StudentService(),
    private val teacherService: TeacherService = TeacherService(),
    private val adminService: AdminService = AdminService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun cargarDatos(rol: Rol) {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (rol) {
                Rol.ESTUDIANTE -> {
                    val progressResult = studentService.getProgress()
                    val activitiesResult = studentService.getActivities()

                    if (progressResult.isSuccess) {
                        val progress = progressResult.getOrNull()
                        val actividades = activitiesResult.getOrNull() ?: emptyList()
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                studentProgress = progress,
                                actividadesEstudiante = actividades,
                                error = null
                            )
                        }
                    } else {
                        val err = progressResult.exceptionOrNull()?.message ?: "Sin conexión con la API"
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                error = "No se pudo conectar con el servidor para obtener el progreso: $err"
                            )
                        }
                    }
                }

                Rol.DOCENTE -> {
                    val profileResult = teacherService.getProfile()
                    profileResult.onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                teacherProfile = profile,
                                error = null
                            )
                        }
                    }.onFailure { ex ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                error = "No se pudo conectar con el servidor para obtener el perfil del docente: ${ex.message}"
                            )
                        }
                    }
                }

                Rol.ADMINISTRADOR -> {
                    val statsResult = adminService.getStatistics()
                    statsResult.onSuccess { stats ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                adminStatistics = stats,
                                error = null
                            )
                        }
                    }.onFailure { ex ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                error = "No se pudo conectar con el servidor para obtener las estadísticas: ${ex.message}"
                            )
                        }
                    }
                }
            }
        }
    }
}
