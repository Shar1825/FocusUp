package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.*
import java.sql.Date
import java.sql.Statement
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal data class InternalSesion(
    val id: Int,
    val estudianteId: Int,
    val objetivo: String,
    val duracionMin: Int,
    val fecha: LocalDate = LocalDate.now(),
    var estado: String = "EN_CURSO"
)

internal data class InternalEvidencia(
    val id: Int,
    val sesionId: Int,
    val imagen: ByteArray?,
    var estado: String = "PENDIENTE",
    var comentario: String? = null,
    val fecha: LocalDate = LocalDate.now()
)

/** Acceso a MySQL para sesiones de estudio y evidencias con respaldo en memoria. */
object EstudioRepository {

    private val sesionesMap = ConcurrentHashMap<Int, InternalSesion>()
    private val evidenciasMap = ConcurrentHashMap<Int, InternalEvidencia>()
    private val nextSesionId = AtomicInteger(1)
    private val nextEvidenciaId = AtomicInteger(1)

    private fun ds() = DatabaseFactory.dataSource

    fun crearSesion(req: SesionRequest): SesionResponse {
        val ds = ds()
        if (ds == null) {
            val id = nextSesionId.getAndIncrement()
            val sesion = InternalSesion(id, req.estudianteId, req.objetivo, req.duracionMin)
            sesionesMap[id] = sesion
            return SesionResponse(id, "EN_CURSO")
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "INSERT INTO sesiones (estudiante_id, objetivo, duracion_min, fecha, estado) VALUES (?, ?, ?, NOW(), 'EN_CURSO')",
                Statement.RETURN_GENERATED_KEYS
            ).use { ps ->
                ps.setInt(1, req.estudianteId)
                ps.setString(2, req.objetivo)
                ps.setInt(3, req.duracionMin)
                ps.executeUpdate()
                ps.generatedKeys.use { rs -> rs.next(); SesionResponse(rs.getInt(1), "EN_CURSO") }
            }
        }
    }

    fun crearEvidencia(sesionId: Int, imagen: ByteArray): EvidenciaEstado {
        val ds = ds()
        if (ds == null) {
            val id = nextEvidenciaId.getAndIncrement()
            val evidencia = InternalEvidencia(id, sesionId, imagen)
            evidenciasMap[id] = evidencia
            sesionesMap[sesionId]?.estado = "PENDIENTE_VALIDACION"
            return EvidenciaEstado(id, "PENDIENTE")
        }
        return ds.connection.use { c ->
            var tieneImagenBlob = false
            var tieneRutaImagen = false
            runCatching {
                val meta = c.metaData
                val rs = meta.getColumns(null, null, "evidencias", null)
                while (rs.next()) {
                    val col = rs.getString("COLUMN_NAME").lowercase()
                    if (col == "imagen") tieneImagenBlob = true
                    if (col == "ruta_imagen") tieneRutaImagen = true
                }
                rs.close()
            }

            val sql = when {
                tieneImagenBlob && tieneRutaImagen ->
                    "INSERT INTO evidencias (sesion_id, imagen, ruta_imagen, estado, fecha) VALUES (?, ?, ?, 'PENDIENTE', NOW())"
                tieneImagenBlob ->
                    "INSERT INTO evidencias (sesion_id, imagen, estado, fecha) VALUES (?, ?, 'PENDIENTE', NOW())"
                tieneRutaImagen ->
                    "INSERT INTO evidencias (sesion_id, ruta_imagen, estado, fecha) VALUES (?, ?, 'PENDIENTE', NOW())"
                else ->
                    "INSERT INTO evidencias (sesion_id, estado, fecha) VALUES (?, 'PENDIENTE', NOW())"
            }

            val id = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var idx = 1
                ps.setInt(idx++, sesionId)
                if (tieneImagenBlob && tieneRutaImagen) {
                    ps.setBytes(idx++, imagen)
                    ps.setString(idx++, "evidencia_$sesionId.jpg")
                } else if (tieneImagenBlob) {
                    ps.setBytes(idx++, imagen)
                } else if (tieneRutaImagen) {
                    ps.setString(idx++, "evidencia_$sesionId.jpg")
                }
                ps.executeUpdate()
                ps.generatedKeys.use { rs -> rs.next(); rs.getInt(1) }
            }

            c.prepareStatement("UPDATE sesiones SET estado = 'PENDIENTE_VALIDACION' WHERE id = ?").use { ps ->
                ps.setInt(1, sesionId)
                ps.executeUpdate()
            }
            EvidenciaEstado(id, "PENDIENTE")
        }
    }

    fun pendientes(): List<EvidenciaResumen> {
        val ds = ds()
        if (ds == null) {
            return evidenciasMap.values.filter { it.estado == "PENDIENTE" }.mapNotNull { e ->
                val sesion = sesionesMap[e.sesionId] ?: return@mapNotNull null
                val usuario = UsuarioRepository.buscarPorId(sesion.estudianteId)
                EvidenciaResumen(e.id, e.sesionId, usuario?.nombre ?: "Estudiante #${sesion.estudianteId}", sesion.objetivo, e.estado)
            }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT e.id, e.sesion_id, u.nombre, s.objetivo, e.estado FROM evidencias e " +
                        "JOIN sesiones s ON s.id = e.sesion_id JOIN usuarios u ON u.id = s.estudiante_id " +
                        "WHERE e.estado = 'PENDIENTE' ORDER BY e.fecha"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) add(
                            EvidenciaResumen(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5))
                        )
                    }
                }
            }
        }
    }

    fun estado(id: Int): EvidenciaEstado? {
        val ds = ds()
        if (ds == null) {
            val e = evidenciasMap[id] ?: return null
            return EvidenciaEstado(e.id, e.estado, e.comentario)
        }
        return ds.connection.use { c ->
            c.prepareStatement("SELECT id, estado, comentario FROM evidencias WHERE id = ?").use { ps ->
                ps.setInt(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) EvidenciaEstado(rs.getInt(1), rs.getString(2), rs.getString(3)) else null
                }
            }
        }
    }

    fun imagen(id: Int): ByteArray? {
        val ds = ds()
        if (ds == null) return evidenciasMap[id]?.imagen
        return ds.connection.use { c ->
            var tieneImagen = false
            runCatching {
                val meta = c.metaData
                val rs = meta.getColumns(null, null, "evidencias", "imagen")
                tieneImagen = rs.next()
                rs.close()
            }

            if (tieneImagen) {
                c.prepareStatement("SELECT imagen FROM evidencias WHERE id = ?").use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs -> if (rs.next()) rs.getBytes(1) else null }
                }
            } else null
        }
    }

    /** Aprueba o rechaza una evidencia y actualiza el estado de su sesión. */
    fun validar(id: Int, estado: String, comentario: String?): Boolean {
        val ds = ds()
        if (ds == null) {
            val e = evidenciasMap[id] ?: return false
            e.estado = estado
            e.comentario = comentario
            sesionesMap[e.sesionId]?.estado = if (estado == "APROBADA") "VALIDADA" else "RECHAZADA"
            return true
        }
        return ds.connection.use { c ->
            val filas = c.prepareStatement("UPDATE evidencias SET estado = ?, comentario = ? WHERE id = ?").use { ps ->
                ps.setString(1, estado)
                ps.setString(2, comentario)
                ps.setInt(3, id)
                ps.executeUpdate()
            }
            if (filas > 0) {
                c.prepareStatement(
                    "UPDATE sesiones SET estado = ? WHERE id = (SELECT sesion_id FROM evidencias WHERE id = ?)"
                ).use { ps ->
                    ps.setString(1, if (estado == "APROBADA") "VALIDADA" else "RECHAZADA")
                    ps.setInt(2, id)
                    ps.executeUpdate()
                }
            }
            filas > 0
        }
    }

    /** Minutos de estudio registrados en el día actual para un estudiante. */
    fun obtenerMinutosEstudioHoy(estudianteId: Int): Int {
        val ds = ds()
        if (ds == null) {
            val hoy = LocalDate.now()
            return sesionesMap.values
                .filter { it.estudianteId == estudianteId && it.fecha == hoy && it.estado != "RECHAZADA" }
                .sumOf { it.duracionMin }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT COALESCE(SUM(duracion_min), 0) FROM sesiones WHERE estudiante_id = ? AND DATE(fecha) = CURDATE() AND estado != 'RECHAZADA'"
            ).use { ps ->
                ps.setInt(1, estudianteId)
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }

    /** Racha de días consecutivos con sesiones validadas. */
    fun obtenerRachaDiasValidados(estudianteId: Int): Int {
        val ds = ds()
        val fechasValidadas: List<LocalDate> = if (ds == null) {
            sesionesMap.values
                .filter { it.estudianteId == estudianteId && it.estado == "VALIDADA" }
                .map { it.fecha }
                .distinct()
                .sortedDescending()
        } else {
            ds.connection.use { c ->
                c.prepareStatement(
                    "SELECT DISTINCT DATE(fecha) AS fecha_dia FROM sesiones WHERE estudiante_id = ? AND estado = 'VALIDADA' ORDER BY fecha_dia DESC"
                ).use { ps ->
                    ps.setInt(1, estudianteId)
                    ps.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) {
                                val d = rs.getDate("fecha_dia")
                                if (d != null) add(d.toLocalDate())
                            }
                        }
                    }
                }
            }
        }

        if (fechasValidadas.isEmpty()) return 0
        val hoy = LocalDate.now()
        val ayer = hoy.minusDays(1)

        if (!fechasValidadas.contains(hoy) && !fechasValidadas.contains(ayer)) {
            return 0
        }

        var racha = 0
        var fechaEsperada = if (fechasValidadas.contains(hoy)) hoy else ayer
        val setFechas = fechasValidadas.toSet()

        while (setFechas.contains(fechaEsperada)) {
            racha++
            fechaEsperada = fechaEsperada.minusDays(1)
        }

        return racha
    }

    /** Cantidad de evidencias pendientes para estudiantes de los grupos asignados a un docente. */
    fun contarEvidenciasPendientesDocente(docenteId: Int): Int {
        val ds = ds()
        if (ds == null) {
            val estudianteIds = GrupoRepository.obtenerEstudiantesDeGruposDocente(docenteId).map { it.id }.toSet()
            return evidenciasMap.values.count { e ->
                e.estado == "PENDIENTE" && sesionesMap[e.sesionId]?.estudianteId in estudianteIds
            }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT COUNT(DISTINCT e.id) FROM evidencias e " +
                        "JOIN sesiones s ON s.id = e.sesion_id " +
                        "JOIN grupo_estudiantes ge ON ge.estudiante_id = s.estudiante_id " +
                        "JOIN grupos g ON g.id = ge.grupo_id " +
                        "WHERE g.docente_id = ? AND e.estado = 'PENDIENTE'"
            ).use { ps ->
                ps.setInt(1, docenteId)
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }

    /** Total de sesiones de estudio registradas en el sistema. */
    fun contarTotalSesiones(): Int {
        val ds = ds()
        if (ds == null) return sesionesMap.size
        return ds.connection.use { c ->
            c.prepareStatement("SELECT COUNT(*) FROM sesiones").use { ps ->
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }

    /** Total de evidencias por estado ('PENDIENTE', 'APROBADA', 'RECHAZADA'). */
    fun contarEvidenciasPorEstado(estado: String): Int {
        val ds = ds()
        if (ds == null) return evidenciasMap.values.count { it.estado.equals(estado, ignoreCase = true) }
        return ds.connection.use { c ->
            c.prepareStatement("SELECT COUNT(*) FROM evidencias WHERE UPPER(estado) = UPPER(?)").use { ps ->
                ps.setString(1, estado)
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }
    }
}
