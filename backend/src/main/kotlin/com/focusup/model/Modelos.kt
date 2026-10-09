package com.focusup.model

import kotlinx.serialization.Serializable

/** Roles de la aplicación. Cada rol tiene su propia vista en el frontend. */
@Serializable
enum class Rol {
    ADMINISTRADOR,
    DOCENTE,
    ESTUDIANTE
}

/** Usuario completo, tal como está en usuarios.json (incluye la contraseña). Nunca se envía al cliente. */
@Serializable
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol
)

/** Cuerpo de la petición POST /api/login. */
@Serializable
data class LoginRequest(
    val correo: String,
    val password: String
)

/** Respuesta exitosa del login: los datos del usuario SIN la contraseña. */
@Serializable
data class LoginResponse(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol
)

/** Cuerpo de las respuestas de error (401 y 400). */
@Serializable
data class ErrorResponse(
    val mensaje: String
)