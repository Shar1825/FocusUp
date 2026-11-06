package com.focusup.dto

import com.focusup.model.Rol
import kotlinx.serialization.Serializable

@Serializable
data class TeacherInfo(
    val id: Int,
    val nombre: String,
    val correo: String,
    val grupoNombre: String
)
