package com.focusup.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val nombre: String? = null,
    val correo: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val passwordActual: String,
    val nuevaPassword: String
)
