package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.Group
import com.focusup.model.GroupMember
import com.focusup.model.Usuario
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object GrupoRepository {
    private val gruposMap = ConcurrentHashMap<Int, Group>()
    private val miembrosMap = ConcurrentHashMap<Pair<Int, Int>, GroupMember>()
    private val nextId = AtomicInteger(1)

    init {
        // Datos iniciales de demostración para pruebas e integración en memoria
        val g1 = Group(1, "Matemáticas Focus", "MAT-101", 2, "Docente FocusUp", "Grupo de estudio de matemáticas", 1)
        val g2 = Group(2, "Programación Kotlin", "KOT-202", 2, "Docente FocusUp", "Grupo de desarrollo backend Ktor", 1)
        gruposMap[1] = g1
        gruposMap[2] = g2
        miembrosMap[Pair(1, 3)] = GroupMember(1, 3)
        miembrosMap[Pair(2, 3)] = GroupMember(2, 3)
        nextId.set(3)
    }

    fun obtenerTodos(): List<Group> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return gruposMap.values.toList().sortedBy { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT g.id, g.nombre, g.codigo, g.docente_id, u.nombre AS docenteNombre, g.descripcion, " +
                        "(SELECT COUNT(*) FROM grupo_estudiantes ge WHERE ge.grupo_id = g.id) AS cantidad " +
                        "FROM grupos g LEFT JOIN usuarios u ON u.id = g.docente_id ORDER BY g.id"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Group(
                                    id = rs.getInt("id"),
                                    nombre = rs.getString("nombre"),
                                    codigo = rs.getString("codigo"),
                                    docenteId = rs.getInt("docente_id"),
                                    docenteNombre = rs.getString("docenteNombre") ?: "",
                                    descripcion = rs.getString("descripcion") ?: "",
                                    cantidadEstudiantes = rs.getInt("cantidad")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun obtenerPorDocenteId(docenteId: Int): List<Group> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return gruposMap.values.filter { it.docenteId == docenteId }.sortedBy { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT g.id, g.nombre, g.codigo, g.docente_id, u.nombre AS docenteNombre, g.descripcion, " +
                        "(SELECT COUNT(*) FROM grupo_estudiantes ge WHERE ge.grupo_id = g.id) AS cantidad " +
                        "FROM grupos g LEFT JOIN usuarios u ON u.id = g.docente_id WHERE g.docente_id = ? ORDER BY g.id"
            ).use { ps ->
                ps.setInt(1, docenteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Group(
                                    id = rs.getInt("id"),
                                    nombre = rs.getString("nombre"),
                                    codigo = rs.getString("codigo"),
                                    docenteId = rs.getInt("docente_id"),
                                    docenteNombre = rs.getString("docenteNombre") ?: "",
                                    descripcion = rs.getString("descripcion") ?: "",
                                    cantidadEstudiantes = rs.getInt("cantidad")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun obtenerPorEstudianteId(estudianteId: Int): List<Group> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            val grupoIds = miembrosMap.values.filter { it.estudianteId == estudianteId }.map { it.grupoId }
            return gruposMap.values.filter { it.id in grupoIds }.sortedBy { it.id }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT g.id, g.nombre, g.codigo, g.docente_id, u.nombre AS docenteNombre, g.descripcion, " +
                        "(SELECT COUNT(*) FROM grupo_estudiantes ge WHERE ge.grupo_id = g.id) AS cantidad " +
                        "FROM grupos g JOIN grupo_estudiantes ge ON ge.grupo_id = g.id " +
                        "LEFT JOIN usuarios u ON u.id = g.docente_id WHERE ge.estudiante_id = ? ORDER BY g.id"
            ).use { ps ->
                ps.setInt(1, estudianteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Group(
                                    id = rs.getInt("id"),
                                    nombre = rs.getString("nombre"),
                                    codigo = rs.getString("codigo"),
                                    docenteId = rs.getInt("docente_id"),
                                    docenteNombre = rs.getString("docenteNombre") ?: "",
                                    descripcion = rs.getString("descripcion") ?: "",
                                    cantidadEstudiantes = rs.getInt("cantidad")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun obtenerEstudiantesDeGruposDocente(docenteId: Int): List<Usuario> {
        val gruposDocente = obtenerPorDocenteId(docenteId).map { it.id }
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            val estudianteIds = miembrosMap.values.filter { it.grupoId in gruposDocente }.map { it.estudianteId }.toSet()
            return estudianteIds.mapNotNull { UsuarioRepository.buscarPorId(it) }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT DISTINCT u.id, u.nombre, u.correo, u.password, u.rol, u.activo FROM usuarios u " +
                        "JOIN grupo_estudiantes ge ON ge.estudiante_id = u.id " +
                        "JOIN grupos g ON g.id = ge.grupo_id WHERE g.docente_id = ?"
            ).use { ps ->
                ps.setInt(1, docenteId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Usuario(
                                    id = rs.getInt(1),
                                    nombre = rs.getString(2),
                                    correo = rs.getString(3),
                                    password = rs.getString(4),
                                    rol = com.focusup.model.Rol.valueOf(rs.getString(5)),
                                    activo = rs.getBoolean(6)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun contarTotal(): Int {
        val ds = DatabaseFactory.dataSource
        if (ds == null) return gruposMap.size
        return ds.connection.use { c ->
            c.prepareStatement("SELECT COUNT(*) FROM grupos").use { ps ->
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }
}
