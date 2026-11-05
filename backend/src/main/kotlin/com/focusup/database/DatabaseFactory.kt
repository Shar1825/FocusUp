package com.focusup.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

/**
 * Pool de conexiones a MySQL. Las credenciales vienen de variables de entorno
 * (DB_URL, DB_USER, DB_PASSWORD), nunca del código. Si no existen, dataSource es null
 * y el servidor usa usuarios.json (así funcionan las pruebas sin base de datos).
 */
object DatabaseFactory {
    val dataSource: HikariDataSource? by lazy {
        val url = System.getenv("DB_URL") ?: return@lazy null
        runCatching {
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = url
                username = System.getenv("DB_USER")
                password = System.getenv("DB_PASSWORD")
                maximumPoolSize = 5
                connectionTimeout = 10_000
            })
        }.onFailure { println("No se pudo conectar a MySQL: ${it.message}") }
            .getOrNull()
    }
}