package com.focusup.dto

import com.focusup.model.Rol
import kotlinx.serialization.Serializable

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
    val rol: Rol? = null,
    val password: String? = null
)

@Serializable
data class UpdateUserStatusRequest(
    val activo: Boolean
)

@Serializable
data class AdminStatisticsResponse(
    val totalUsuarios: Int,
    val cantidadAdmin: Int,
    val cantidadDocentes: Int,
    val cantidadEstudiantes: Int,
    val usuariosActivos: Int,
    val usuariosInactivos: Int,
    val totalGrupos: Int,
    val totalActividades: Int
)
