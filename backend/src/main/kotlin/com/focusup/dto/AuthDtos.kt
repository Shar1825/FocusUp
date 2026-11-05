package com.focusup.dto

import com.focusup.model.Rol
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseData(
    val id: Int,
    val name: String,
    val email: String,
    val role: Rol,
    val active: Boolean = true
)

@Serializable
data class AuthSuccessResponse(
    val success: Boolean = true,
    val message: String = "Inicio de sesión correcto",
    val user: LoginResponseData,
    val token: String
)
