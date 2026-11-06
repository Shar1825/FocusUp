package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.AcademicProgress
import com.focusup.model.Grade
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object CalificacionRepository {
    private val calificacionesMap = ConcurrentHashMap<Int, Grade>()
    private val nextId = AtomicInteger(1)

    init {
        // Calificación semilla de prueba
        val g1 = Grade(
            id = 1,
            actividadId = 1,
            estudianteId = 3,
            calificacion = 95.0,
            observaciones = "Excelente trabajo en los ejercicios de álgebra",
            fecha = "2026-11-05",
            actividadTitulo = "Ejercicios de Álgebra",
            estudianteNombre = "Estudiante FocusUp"
        )
        calificacionesMap[1] = g1
        nextId.set(2)
    }

    fun registrarOActualizarCalificacion(
        docenteId: Int,
        actividadId: Int,
        estudianteId: Int,
        calificacion: Double,
        observaciones: String
    ): Grade {
        val actividad = ActividadRepository.buscarPorId(actividadId)
            ?: throw IllegalArgumentException("La actividad #$actividadId no existe.")

        if (actividad.docenteId != docenteId) {
            throw IllegalArgumentException("No tiene permisos para calificar esta actividad.")
        }

        val estudiante = UsuarioRepository.buscarPorId(estudianteId)
            ?: throw IllegalArgumentException("El estudiante #$estudianteId no existe.")

        val existente = calificacionesMap.values.find { it.actividadId == actividadId && it.estudianteId == estudianteId }
        val ds = DatabaseFactory.dataSource

        if (ds == null) {
            val id = existente?.id ?: nextId.getAndIncrement()
            val nuevaGrade = Grade(
                id = id,
                actividadId = actividadId,
                estudianteId = estudianteId,
                calificacion = calificacion,
                observaciones = observaciones,
                fecha = "2026-11-05",
                actividadTitulo = actividad.titulo,
                estudianteNombre = estudiante.nombre
            )
            calificacionesMap[id] = nuevaGrade
            return nuevaGrade
        }

        return ds.connection.use { c ->
            c.prepareStatement(
                "INSERT INTO calificaciones (actividad_id, estudiante_id, calificacion, observaciones, fecha) " +
                        "VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE calificacion = VALUES(calificacion), observaciones = VALUES(observaciones), fecha = NOW()",
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { ps ->
                ps.setInt(1, actividadId)
                ps.setInt(2, estudianteId)
                ps.setDouble(3, calificacion)
                ps.setString(4, observaciones)
                ps.executeUpdate()
                ps.generatedKeys.use { rs ->
                    val id = if (rs.next()) rs.getInt(1) else existente?.id ?: 1
                    Grade(id, actividadId, estudianteId, calificacion, observaciones, "2026-11-05", actividad.titulo, estudiante.nombre)
                }
            }
        }
    }

    fun obtenerPorDocenteId(docenteId: Int): List<Grade> {
        val actividadesDocente = ActividadRepository.obtenerPorDocenteId(docenteId).map { it.id }
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return calificacionesMap.values.filter { it.actividadId in actividadesDocente }.sortedByDescending { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT c.id, c.actividad_id, c.estudiante_id, c.calificacion, c.observaciones, c.fecha, " +
                        "a.titulo AS actividadTitulo, u.nombre AS estudianteNombre FROM calificaciones c " +
                        "JOIN actividades a ON a.id = c.actividad_id JOIN usuarios u ON u.id = c.estudiante_id " +
                        "WHERE a.docente_id = ? ORDER BY c.id DESC"
            ).use { ps ->
                ps.setInt(1, docenteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Grade(
                                    id = rs.getInt(1),
                                    actividadId = rs.getInt(2),
                                    estudianteId = rs.getInt(3),
                                    calificacion = rs.getDouble(4),
                                    observaciones = rs.getString(5) ?: "",
                                    fecha = rs.getString(6) ?: "",
                                    actividadTitulo = rs.getString(7) ?: "",
                                    estudianteNombre = rs.getString(8) ?: ""
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun obtenerPorEstudianteId(estudianteId: Int): List<Grade> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return calificacionesMap.values.filter { it.estudianteId == estudianteId }.sortedByDescending { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT c.id, c.actividad_id, c.estudiante_id, c.calificacion, c.observaciones, c.fecha, " +
                        "a.titulo AS actividadTitulo, u.nombre AS estudianteNombre FROM calificaciones c " +
                        "JOIN actividades a ON a.id = c.actividad_id JOIN usuarios u ON u.id = c.estudiante_id " +
                        "WHERE c.estudiante_id = ? ORDER BY c.id DESC"
            ).use { ps ->
                ps.setInt(1, estudianteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Grade(
                                    id = rs.getInt(1),
                                    actividadId = rs.getInt(2),
                                    estudianteId = rs.getInt(3),
                                    calificacion = rs.getDouble(4),
                                    observaciones = rs.getString(5) ?: "",
                                    fecha = rs.getString(6) ?: "",
                                    actividadTitulo = rs.getString(7) ?: "",
                                    estudianteNombre = rs.getString(8) ?: ""
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun calcularProgresoEstudiante(estudianteId: Int): AcademicProgress {
        val estudiante = UsuarioRepository.buscarPorId(estudianteId)
        val actividades = ActividadRepository.obtenerPorEstudianteId(estudianteId)
        val calificaciones = obtenerPorEstudianteId(estudianteId)

        val totalActividades = actividades.size
        val completadas = calificaciones.size
        val pendientes = (totalActividades - completadas).coerceAtLeast(0)
        val promedio = if (completadas > 0) calificaciones.map { it.calificacion }.average() else 0.0

        return AcademicProgress(
            estudianteId = estudianteId,
            estudianteNombre = estudiante?.nombre ?: "Estudiante #$estudianteId",
            actividadesTotales = totalActividades,
            actividadesCompletadas = completadas,
            actividadesPendientes = pendientes,
            promedioCalificaciones = (Math.round(promedio * 100.0) / 100.0),
            porcentajeAsistencia = 95.0
        )
    }
}
