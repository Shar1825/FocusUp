package com.focusup.model

import kotlinx.serialization.Serializable

/** Modelo de Grupo Académico. */
@Serializable
data class Group(
    val id: Int,
    val nombre: String,
    val codigo: String,
    val docenteId: Int,
    val docenteNombre: String = "",
    val descripcion: String = "",
    val cantidadEstudiantes: Int = 0
)

/** Relación Estudiante-Grupo. */
@Serializable
data class GroupMember(
    val grupoId: Int,
    val estudianteId: Int
)

/** Modelo de Actividad Académica. */
@Serializable
data class Activity(
    val id: Int,
    val grupoId: Int,
    val docenteId: Int,
    val titulo: String,
    val descripcion: String,
    val fechaEntrega: String,
    val grupoNombre: String = ""
)

/** Modelo de Calificación. */
@Serializable
data class Grade(
    val id: Int,
    val actividadId: Int,
    val estudianteId: Int,
    val calificacion: Double,
    val observaciones: String = "",
    val fecha: String = "",
    val actividadTitulo: String = "",
    val estudianteNombre: String = ""
)

/** Seguimiento al Progreso Académico de un estudiante. */
@Serializable
data class AcademicProgress(
    val estudianteId: Int,
    val estudianteNombre: String,
    val actividadesTotales: Int = 0,
    val actividadesCompletadas: Int = 0,
    val actividadesPendientes: Int = 0,
    val promedioCalificaciones: Double = 0.0,
    val porcentajeAsistencia: Double = 100.0
)
