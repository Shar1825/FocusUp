package com.focusup.database

object DatabaseInitializer {
    fun initSchema() {
        val ds = DatabaseFactory.dataSource ?: return
        runCatching {
            ds.connection.use { c ->
                c.createStatement().use { s ->
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
                }
            }
        }.onFailure {
            println("Aviso inicializando esquema MySQL: ${it.message}")
        }
    }
}
