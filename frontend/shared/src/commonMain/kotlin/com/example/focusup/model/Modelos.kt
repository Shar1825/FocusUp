package com.example.focusup.model


import kotlinx.serialization.Serializable

/** Roles soportados por el sistema FocusUp */
@Serializable
enum class Rol {
    ADMINISTRADOR,
    DOCENTE,
    ESTUDIANTE
}

/** Modelo estándar para respuestas del backend Ktor */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
)

/** Autenticación */
@Serializable
data class LoginRequest(
    val correo: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val token: String? = null
)

@Serializable
data class ErrorResponse(
    val mensaje: String = "Ha ocurrido un error en la solicitud"
)

/** Perfiles */
@Serializable
data class StudentProfile(
    val id: Int = 0,
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.ESTUDIANTE,
    val matricula: String = "EST-1001",
    val promedioGeneral: Double = 0.0,
    val actividadesCompletadas: Int = 0
)

@Serializable
data class TeacherProfile(
    val id: Int = 0,
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.DOCENTE,
    val especialidad: String = "General",
    val gruposAsignadosCount: Int = 0,
    val totalEstudiantesCount: Int = 0
)

/** Grupos y Docentes */
@Serializable
data class Group(
    val id: Int,
    val nombre: String,
    val codigo: String = "",
    val docenteId: Int = 0,
    val docenteNombre: String = "",
    val descripcion: String = "",
    val cantidadEstudiantes: Int = 0
)

@Serializable
data class TeacherInfo(
    val id: Int,
    val nombre: String,
    val correo: String,
    val grupoNombre: String = ""
)

/** Actividades y Tareas Académicas */
@Serializable
data class Activity(
    val id: Int,
    val grupoId: Int = 0,
    val docenteId: Int = 0,
    val titulo: String,
    val descripcion: String = "",
    val fechaEntrega: String = "",
    val grupoNombre: String = ""
)

@Serializable
data class CreateActivityRequest(
    val grupoId: Int,
    val titulo: String,
    val descripcion: String,
    val fechaEntrega: String
)

@Serializable
data class UpdateActivityRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val fechaEntrega: String? = null
)

/** Calificaciones */
@Serializable
data class Grade(
    val id: Int = 0,
    val actividadId: Int = 0,
    val estudianteId: Int = 0,
    val calificacion: Double = 0.0,
    val observaciones: String = "",
    val fecha: String = "",
    val actividadTitulo: String = "",
    val estudianteNombre: String = ""
)

@Serializable
data class RegisterGradeRequest(
    val actividadId: Int,
    val estudianteId: Int,
    val calificacion: Double,
    val observaciones: String = ""
)

/** Progreso Académico */
@Serializable
data class AcademicProgress(
    val estudianteId: Int = 0,
    val estudianteNombre: String = "",
    val actividadesTotales: Int = 0,
    val actividadesCompletadas: Int = 0,
    val actividadesPendientes: Int = 0,
    val promedioCalificaciones: Double = 0.0,
    val porcentajeAsistencia: Double = 100.0
)

/** Administración */
@Serializable
data class UserDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean = true
)

@Serializable
data class CreateUserRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol
)

@Serializable
data class UpdateUserRequest(
    val nombre: String? = null,
    val correo: String? = null,
    val password: String? = null,
    val rol: Rol? = null
)

@Serializable
data class UpdateUserStatusRequest(
    val activo: Boolean
)

@Serializable
data class AdminStatistics(
    val totalUsuarios: Int = 0,
    val estudiantesCount: Int = 0,
    val docentesCount: Int = 0,
    val administradoresCount: Int = 0,
    val gruposTotales: Int = 0,
    val actividadesTotales: Int = 0
)

/** Sesiones de Estudio y Evidencias */
@Serializable
data class SesionRequest(
    val estudianteId: Int,
    val objetivo: String,
    val duracionMin: Int
)

@Serializable
data class SesionResponse(
    val id: Int,
    val estado: String = "ACTIVA"
)

@Serializable
data class EvidenciaEstado(
    val id: Int,
    val estado: String,
    val comentario: String? = null
)

@Serializable
data class EvidenciaResumen(
    val id: Int,
    val sesionId: Int,
    val estudiante: String = "",
    val objetivo: String = "",
    val estado: String = "PENDIENTE",
    @kotlinx.serialization.Transient
    val imagenBytes: ByteArray? = null
)

@Serializable
data class ValidarRequest(
    val estado: String,
    val comentario: String? = null
)
