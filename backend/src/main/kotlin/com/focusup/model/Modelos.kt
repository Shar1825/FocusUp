package com.focusup.model

import kotlinx.serialization.Serializable

/** Roles del sistema FocusUp. */
@Serializable
enum class Rol {
    ADMINISTRADOR,
    ADMIN,
    DOCENTE,
    ESTUDIANTE;

    fun esAdmin(): Boolean = this == ADMINISTRADOR || this == ADMIN
}

/** Objeto Usuario completo (almacenado en DB/JSON). Contraseña en hash/protegida. */
@Serializable
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol,
    val activo: Boolean = true
)

/** Resumen de usuario seguro para ser devuelto al frontend (sin contraseña). */
@Serializable
data class UserDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean = true
)

/** Perfil extendido de Administrador. */
@Serializable
data class AdminProfile(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol = Rol.ADMINISTRADOR,
    val departamento: String = "Administración Central",
    val ultimoAcceso: String? = null
)

/** Perfil extendido de Docente. */
@Serializable
data class TeacherProfile(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol = Rol.DOCENTE,
    val especialidad: String = "General",
    val gruposAsignadosCount: Int = 0,
    val totalEstudiantesCount: Int = 0
)

/** Perfil extendido de Estudiante. */
@Serializable
data class StudentProfile(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol = Rol.ESTUDIANTE,
    val matricula: String = "EST-${1000}",
    val promedioGeneral: Double = 0.0,
    val actividadesCompletadas: Int = 0
)

/** Petición de inicio de sesión. Soporta tanto "correo" como "email". */
@Serializable
data class LoginRequest(
    val correo: String = "",
    val password: String = "",
    val email: String? = null
) {
    val correoEfectivo: String get() = email?.takeIf { it.isNotBlank() } ?: correo
}

/** Respuesta exitosa del login (manteniendo retrocompatibilidad). */
@Serializable
data class LoginResponse(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol
)

/** Respuesta de error legacy. */
@Serializable
data class ErrorResponse(
    val mensaje: String
)
