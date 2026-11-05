
package com.focusup.model

import kotlinx.serialization.Serializable

@Serializable
data class SesionRequest(val estudianteId: Int, val objetivo: String, val duracionMin: Int)

@Serializable
data class SesionResponse(val id: Int, val estado: String)

@Serializable
data class EvidenciaEstado(val id: Int, val estado: String, val comentario: String? = null)

@Serializable
data class EvidenciaResumen(
    val id: Int,
    val sesionId: Int,
    val estudiante: String,
    val objetivo: String,
    val estado: String
)

@Serializable
data class ValidarRequest(val estado: String, val comentario: String? = null)