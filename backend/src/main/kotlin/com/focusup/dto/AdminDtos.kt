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
    val usuariosActivosAdmin: Int = 0,
    val usuariosActivosDocente: Int = 0,
    val usuariosActivosEstudiante: Int = 0,
    val totalGrupos: Int,
    val totalActividades: Int,
    val totalSesiones: Int = 0,
    val evidenciasPendientes: Int = 0,
    val evidenciasAprobadas: Int = 0,
    val evidenciasRechazadas: Int = 0
)
