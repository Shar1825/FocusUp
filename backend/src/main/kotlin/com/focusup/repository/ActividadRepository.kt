package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.Activity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object ActividadRepository {
    private val actividadesMap = ConcurrentHashMap<Int, Activity>()
    private val nextId = AtomicInteger(1)

    init {
        // Datos semilla iniciales
        val a1 = Activity(1, 1, 2, "Ejercicios de Álgebra", "Resolver del 1 al 10", "2026-11-20", "Matemáticas Focus")
        val a2 = Activity(2, 2, 2, "Proyecto Ktor Endpoints", "Desarrollar API REST con autenticación JWT", "2026-11-30", "Programación Kotlin")
        actividadesMap[1] = a1
        actividadesMap[2] = a2
        nextId.set(3)
    }

    fun buscarPorId(id: Int): Activity? {
        val ds = DatabaseFactory.dataSource
        if (ds == null) return actividadesMap[id]
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT a.id, a.grupo_id, a.docente_id, a.titulo, a.descripcion, a.fecha_entrega, g.nombre AS grupoNombre " +
                        "FROM actividades a JOIN grupos g ON g.id = a.grupo_id WHERE a.id = ?"
            ).use { ps ->
                ps.setInt(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        Activity(
                            id = rs.getInt("id"),
                            grupoId = rs.getInt("grupo_id"),
                            docenteId = rs.getInt("docente_id"),
                            titulo = rs.getString("titulo"),
                            descripcion = rs.getString("descripcion"),
                            fechaEntrega = rs.getString("fecha_entrega"),
                            grupoNombre = rs.getString("grupoNombre") ?: ""
                        )
                    } else null
                }
            }
        }
    }

    fun obtenerPorDocenteId(docenteId: Int): List<Activity> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) return actividadesMap.values.filter { it.docenteId == docenteId }.sortedByDescending { it.id }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT a.id, a.grupo_id, a.docente_id, a.titulo, a.descripcion, a.fecha_entrega, g.nombre AS grupoNombre " +
                        "FROM actividades a JOIN grupos g ON g.id = a.grupo_id WHERE a.docente_id = ? ORDER BY a.id DESC"
            ).use { ps ->
                ps.setInt(1, docenteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Activity(
                                    id = rs.getInt("id"),
                                    grupoId = rs.getInt("grupo_id"),
                                    docenteId = rs.getInt("docente_id"),
                                    titulo = rs.getString("titulo"),
                                    descripcion = rs.getString("descripcion"),
                                    fechaEntrega = rs.getString("fecha_entrega"),
                                    grupoNombre = rs.getString("grupoNombre") ?: ""
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun obtenerPorEstudianteId(estudianteId: Int): List<Activity> {
        val gruposEstudiante = GrupoRepository.obtenerPorEstudianteId(estudianteId).map { it.id }
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return actividadesMap.values.filter { it.grupoId in gruposEstudiante }.sortedByDescending { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT a.id, a.grupo_id, a.docente_id, a.titulo, a.descripcion, a.fecha_entrega, g.nombre AS grupoNombre " +
                        "FROM actividades a JOIN grupos g ON g.id = a.grupo_id " +
                        "JOIN grupo_estudiantes ge ON ge.grupo_id = g.id WHERE ge.estudiante_id = ? ORDER BY a.id DESC"
            ).use { ps ->
                ps.setInt(1, estudianteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Activity(
                                    id = rs.getInt("id"),
                                    grupoId = rs.getInt("grupo_id"),
                                    docenteId = rs.getInt("docente_id"),
                                    titulo = rs.getString("titulo"),
                                    descripcion = rs.getString("descripcion"),
                                    fechaEntrega = rs.getString("fecha_entrega"),
                                    grupoNombre = rs.getString("grupoNombre") ?: ""
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun crearActividad(docenteId: Int, grupoId: Int, titulo: String, descripcion: String, fechaEntrega: String): Activity {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            val id = nextId.getAndIncrement()
            val grupoNombre = GrupoRepository.obtenerTodos().find { it.id == grupoId }?.nombre ?: "Grupo #$grupoId"
            val nueva = Activity(id, grupoId, docenteId, titulo, descripcion, fechaEntrega, grupoNombre)
            actividadesMap[id] = nueva
            return nueva
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "INSERT INTO actividades (grupo_id, docente_id, titulo, descripcion, fecha_entrega) VALUES (?, ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { ps ->
                ps.setInt(1, grupoId)
                ps.setInt(2, docenteId)
                ps.setString(3, titulo)
                ps.setString(4, descripcion)
                ps.setString(5, fechaEntrega)
                ps.executeUpdate()
                ps.generatedKeys.use { rs ->
                    rs.next()
                    val id = rs.getInt(1)
                    val grupoNombre = GrupoRepository.obtenerTodos().find { it.id == grupoId }?.nombre ?: ""
                    Activity(id, grupoId, docenteId, titulo, descripcion, fechaEntrega, grupoNombre)
                }
            }
        }
    }

    fun actualizarActividad(id: Int, docenteId: Int, titulo: String?, descripcion: String?, fechaEntrega: String?): Activity? {
        val actual = buscarPorId(id) ?: return null
        if (actual.docenteId != docenteId) {
            throw IllegalArgumentException("No tiene permisos para modificar esta actividad ya que pertenece a otro docente.")
        }
        val actualizada = actual.copy(
            titulo = titulo?.takeIf { it.isNotBlank() } ?: actual.titulo,
            descripcion = descripcion?.takeIf { it.isNotBlank() } ?: actual.descripcion,
            fechaEntrega = fechaEntrega?.takeIf { it.isNotBlank() } ?: actual.fechaEntrega
        )

        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            actividadesMap[id] = actualizada
            return actualizada
        }
        ds.connection.use { c ->
            c.prepareStatement("UPDATE actividades SET titulo = ?, descripcion = ?, fecha_entrega = ? WHERE id = ?").use { ps ->
                ps.setString(1, actualizada.titulo)
                ps.setString(2, actualizada.descripcion)
                ps.setString(3, actualizada.fechaEntrega)
                ps.setInt(4, id)
                ps.executeUpdate()
            }
        }
        return actualizada
    }

    fun contarTotal(): Int {
        val ds = DatabaseFactory.dataSource
        if (ds == null) return actividadesMap.size
        return ds.connection.use { c ->
            c.prepareStatement("SELECT COUNT(*) FROM actividades").use { ps ->
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }
}
