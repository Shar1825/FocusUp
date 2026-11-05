package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.*
import java.sql.Statement

/** Acceso a MySQL para sesiones de estudio y evidencias. */
object EstudioRepository {

    private fun ds() = DatabaseFactory.dataSource
        ?: throw IllegalStateException("Base de datos no configurada")

    fun crearSesion(req: SesionRequest): SesionResponse = ds().connection.use { c ->
        c.prepareStatement(
            "INSERT INTO sesiones (estudiante_id, objetivo, duracion_min, fecha, estado) " +
                    "VALUES (?, ?, ?, NOW(), 'EN_CURSO')",
            Statement.RETURN_GENERATED_KEYS
        ).use { ps ->
            ps.setInt(1, req.estudianteId)
            ps.setString(2, req.objetivo)
            ps.setInt(3, req.duracionMin)
            ps.executeUpdate()
            ps.generatedKeys.use { rs -> rs.next(); SesionResponse(rs.getInt(1), "EN_CURSO") }
        }
    }

    fun crearEvidencia(sesionId: Int, imagen: ByteArray): EvidenciaEstado = ds().connection.use { c ->
        val id = c.prepareStatement(
            "INSERT INTO evidencias (sesion_id, imagen, estado, fecha) VALUES (?, ?, 'PENDIENTE', NOW())",
            Statement.RETURN_GENERATED_KEYS
        ).use { ps ->
            ps.setInt(1, sesionId)
            ps.setBytes(2, imagen)
            ps.executeUpdate()
            ps.generatedKeys.use { rs -> rs.next(); rs.getInt(1) }
        }
        c.prepareStatement("UPDATE sesiones SET estado = 'PENDIENTE_VALIDACION' WHERE id = ?").use { ps ->
            ps.setInt(1, sesionId)
            ps.executeUpdate()
        }
        EvidenciaEstado(id, "PENDIENTE")
    }

    fun pendientes(): List<EvidenciaResumen> = ds().connection.use { c ->
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

    fun estado(id: Int): EvidenciaEstado? = ds().connection.use { c ->
        c.prepareStatement("SELECT id, estado, comentario FROM evidencias WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) EvidenciaEstado(rs.getInt(1), rs.getString(2), rs.getString(3)) else null
            }
        }
    }

    fun imagen(id: Int): ByteArray? = ds().connection.use { c ->
        c.prepareStatement("SELECT imagen FROM evidencias WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getBytes(1) else null }
        }
    }

    /** Aprueba o rechaza una evidencia y actualiza el estado de su sesión. */
    fun validar(id: Int, estado: String, comentario: String?): Boolean = ds().connection.use { c ->
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
