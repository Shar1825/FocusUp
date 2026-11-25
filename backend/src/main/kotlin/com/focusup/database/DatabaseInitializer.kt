package com.focusup.database

import com.focusup.authentication.PasswordHasher
import com.focusup.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate

object DatabaseInitializer {

    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    fun logDatabaseStatus() {
        val ds = DatabaseFactory.dataSource
        if (ds != null) {
            val dbUrl = System.getenv("DB_URL") ?: ""
            logger.info("==================================================")
            logger.info("🟢 BASE DE DATOS: MODO MYSQL ACTIVO Y CONECTADO")
            logger.info("URL: $dbUrl")
            logger.info("==================================================")
            println("🟢 [FocusUp Server] Base de datos MySQL activa y conectada ($dbUrl).")
        } else {
            logger.info("==================================================")
            logger.info("⚠️ BASE DE DATOS: MODO FALLBACK (JSON / MOCK) ACTIVO")
            logger.info("DB_URL no fue encontrada o la conexión a MySQL falló. Usando usuarios.json.")
            logger.info("==================================================")
            println("⚠️ [FocusUp Server] DB_URL no encontrada o sin conexión. Usando modo JSON de respaldo (usuarios.json).")
        }
    }

    fun initSchema() {
        logDatabaseStatus()
        val ds = DatabaseFactory.dataSource

        if (ds == null) {
            // En modo fallback (sin MySQL), ejecutar migración de contraseñas en memoria
            UsuarioRepository.migrarContrasenasSinHash()
            return
        }

        try {
            ds.connection.use { c ->
                c.createStatement().use { s ->
                    // 1. Tabla usuarios
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS usuarios (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                nombre VARCHAR(100) NOT NULL,
                                correo VARCHAR(100) NOT NULL UNIQUE,
                                password VARCHAR(255) NOT NULL,
                                rol VARCHAR(30) NOT NULL,
                                activo BOOLEAN NOT NULL DEFAULT TRUE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'usuarios': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'usuarios': ${e.message}")
                    }

                    // 2. Tabla grupos
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS grupos (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                nombre VARCHAR(100) NOT NULL,
                                codigo VARCHAR(50) NOT NULL,
                                docente_id INT NOT NULL,
                                descripcion TEXT,
                                FOREIGN KEY (docente_id) REFERENCES usuarios(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'grupos': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'grupos': ${e.message}")
                    }

                    // 3. Tabla grupo_estudiantes
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS grupo_estudiantes (
                                grupo_id INT NOT NULL,
                                estudiante_id INT NOT NULL,
                                PRIMARY KEY (grupo_id, estudiante_id),
                                FOREIGN KEY (grupo_id) REFERENCES grupos(id) ON DELETE CASCADE,
                                FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'grupo_estudiantes': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'grupo_estudiantes': ${e.message}")
                    }

                    // 4. Tabla actividades
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS actividades (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                grupo_id INT NOT NULL,
                                docente_id INT NOT NULL,
                                titulo VARCHAR(150) NOT NULL,
                                descripcion TEXT,
                                fecha_entrega VARCHAR(50) NOT NULL,
                                FOREIGN KEY (grupo_id) REFERENCES grupos(id) ON DELETE CASCADE,
                                FOREIGN KEY (docente_id) REFERENCES usuarios(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'actividades': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'actividades': ${e.message}")
                    }

                    // 5. Tabla calificaciones
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS calificaciones (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                actividad_id INT NOT NULL,
                                estudiante_id INT NOT NULL,
                                calificacion DOUBLE NOT NULL,
                                observaciones TEXT,
                                fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE KEY unq_act_est (actividad_id, estudiante_id),
                                FOREIGN KEY (actividad_id) REFERENCES actividades(id) ON DELETE CASCADE,
                                FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'calificaciones': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'calificaciones': ${e.message}")
                    }

                    // 6. Tabla sesiones (Pomodoro)
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS sesiones (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                estudiante_id INT NOT NULL,
                                objetivo VARCHAR(255) NOT NULL,
                                duracion_min INT NOT NULL,
                                fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                estado VARCHAR(50) NOT NULL DEFAULT 'EN_CURSO',
                                FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'sesiones': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'sesiones': ${e.message}")
                    }

                    // 7. Tabla evidencias (Pomodoro)
                    try {
                        s.execute(
                            """
                            CREATE TABLE IF NOT EXISTS evidencias (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                sesion_id INT NOT NULL,
                                imagen LONGBLOB NULL,
                                ruta_imagen VARCHAR(255) NULL,
                                estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
                                comentario TEXT,
                                fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (sesion_id) REFERENCES sesiones(id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        logger.error("❌ Error creando tabla 'evidencias': ${e.message}", e)
                        println("❌ [FocusUp Server] Error creando tabla 'evidencias': ${e.message}")
                    }
                }

                // Sembrado de Datos Semilla Idempotentes
                sembrarDatosIniciales(c)
            }
        } catch (e: Exception) {
            logger.error("❌ Error general al inicializar esquema MySQL: ${e.message}", e)
            println("❌ [FocusUp Server] Error general al inicializar esquema MySQL: ${e.message}")
        }

        // Migración de contraseñas legacy a BCrypt
        UsuarioRepository.migrarContrasenasSinHash()
    }

    private fun sembrarDatosIniciales(c: java.sql.Connection) {
        runCatching {
            // A. Sembrado de Usuarios por defecto (si la tabla usuarios está vacía)
            val countUsuarios = c.createStatement().use { s ->
                s.executeQuery("SELECT COUNT(*) FROM usuarios").use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            if (countUsuarios == 0) {
                c.prepareStatement(
                    "INSERT INTO usuarios (id, nombre, correo, password, rol, activo) VALUES (?, ?, ?, ?, ?, true)"
                ).use { ps ->
                    val passAdmin = PasswordHasher.hashPassword("admin123")
                    val passDocente = PasswordHasher.hashPassword("docente123")
                    val passEstudiante = PasswordHasher.hashPassword("estudiante123")

                    ps.setInt(1, 1); ps.setString(2, "Administrador FocusUp"); ps.setString(3, "admin@focusup.com"); ps.setString(4, passAdmin); ps.setString(5, "ADMINISTRADOR"); ps.addBatch()
                    ps.setInt(1, 2); ps.setString(2, "Docente FocusUp"); ps.setString(3, "docente@focusup.com"); ps.setString(4, passDocente); ps.setString(5, "DOCENTE"); ps.addBatch()
                    ps.setInt(1, 3); ps.setString(2, "Estudiante FocusUp"); ps.setString(3, "estudiante@focusup.com"); ps.setString(4, passEstudiante); ps.setString(5, "ESTUDIANTE"); ps.addBatch()
                    ps.executeBatch()
                }
                println("🌱 [FocusUp Server] Usuarios iniciales sembrados (id 1, 2, 3).")
            }

            // B. Sembrado de Grupo "Programación Móvil" (docente_id = 2)
            val countGrupos = c.createStatement().use { s ->
                s.executeQuery("SELECT COUNT(*) FROM grupos").use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            if (countGrupos == 0) {
                c.prepareStatement(
                    "INSERT INTO grupos (id, nombre, codigo, docente_id, descripcion) VALUES (1, 'Programación Móvil', 'PROG-MOV-01', 2, 'Curso de desarrollo móvil en KMP y Jetpack Compose')"
                ).use { ps ->
                    ps.executeUpdate()
                }
                println("🌱 [FocusUp Server] Grupo 'Programación Móvil' sembrado con docente id=2.")
            }

            // C. Inscripción del Estudiante id=3 en el grupo id=1
            val countInscripciones = c.createStatement().use { s ->
                s.executeQuery("SELECT COUNT(*) FROM grupo_estudiantes WHERE grupo_id = 1 AND estudiante_id = 3").use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            if (countInscripciones == 0) {
                c.prepareStatement(
                    "INSERT INTO grupo_estudiantes (grupo_id, estudiante_id) VALUES (1, 3)"
                ).use { ps ->
                    ps.executeUpdate()
                }
                println("🌱 [FocusUp Server] Estudiante id=3 inscrito en el grupo id=1.")
            }

            // D. Actividades de ejemplo con fechas futuras relativas calculadas al momento del sembrado
            val countActividades = c.createStatement().use { s ->
                s.executeQuery("SELECT COUNT(*) FROM actividades").use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            if (countActividades == 0) {
                val hoy = LocalDate.now()
                val fechaProyecto = hoy.plusDays(14).toString()
                val fechaTaller = hoy.plusDays(30).toString()

                c.prepareStatement(
                    "INSERT INTO actividades (id, grupo_id, docente_id, titulo, descripcion, fecha_entrega) VALUES (?, 1, 2, ?, ?, ?)"
                ).use { ps ->
                    ps.setInt(1, 1)
                    ps.setString(2, "Proyecto Final KMP FocusUp")
                    ps.setString(3, "Desarrollar el frontend en Kotlin Multiplatform con arquitectura MVVM y cámara.")
                    ps.setString(4, fechaProyecto)
                    ps.addBatch()

                    ps.setInt(1, 2)
                    ps.setString(2, "Taller de Jetpack Compose")
                    ps.setString(3, "Diseño e implementación de componentes e interfaces interactivas.")
                    ps.setString(4, fechaTaller)
                    ps.addBatch()

                    ps.executeBatch()
                }
                println("🌱 [FocusUp Server] Actividades iniciales sembradas con fechas relativas ($fechaProyecto y $fechaTaller).")
            }
        }.onFailure {
            logger.warn("Aviso sembrando datos iniciales en MySQL: ${it.message}")
            println("Aviso sembrando datos iniciales en MySQL: ${it.message}")
        }
    }
}
