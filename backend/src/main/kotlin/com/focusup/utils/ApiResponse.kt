package com.focusup.utils

import kotlinx.serialization.Serializable

/** Respuesta genérica envuelta para la API REST. */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val token: String? = null
)

/** Respuesta rápida de error estándar. */
@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String
)

/** Respuesta de éxito simple. */
@Serializable
data class ApiSuccessResponse(
    val success: Boolean = true,
    val message: String
)
