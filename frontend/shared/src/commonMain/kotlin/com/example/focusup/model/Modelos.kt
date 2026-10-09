package com.example.focusup.model

import kotlinx.serialization.Serializable

@Serializable
enum class Rol {
    ADMINISTRADOR,
    DOCENTE,
    ESTUDIANTE
}

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
    val rol: Rol
)

@Serializable
data class ErrorResponse(
    val mensaje: String
)