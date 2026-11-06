package com.focusup.dto

import kotlinx.serialization.Serializable

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

@Serializable
data class RegisterGradeRequest(
    val actividadId: Int,
    val estudianteId: Int,
    val calificacion: Double,
    val observaciones: String = ""
)
