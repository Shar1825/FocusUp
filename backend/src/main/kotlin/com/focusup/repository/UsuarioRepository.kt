package com.focusup.repository

import com.focusup.database.DatabaseFactory
import com.focusup.model.Rol
import com.focusup.model.Usuario
import kotlinx.serialization.json.Json

/**
 * Repositorio de usuarios. Si hay base de datos MySQL configurada (variables DB_URL, DB_USER,
 * DB_PASSWORD) consulta la tabla usuarios; si no, usa usuarios.json como respaldo
 * (así funcionan las pruebas automáticas sin conexión).
 */
object UsuarioRepository {
    private var usuarios: List<Usuario> = emptyList()

    init {
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        runCatching {
            val jsonString = this::class.java.classLoader
                .getResourceAsStream("usuarios.json")
                ?.bufferedReader()
                ?.readText() ?: throw IllegalStateException("No se encontró usuarios.json")

            val json = Json { ignoreUnknownKeys = true }
            usuarios = json.decodeFromString<List<Usuario>>(jsonString)
        }.onFailure {
            println("Error al cargar usuarios.json: ${it.message}")
        }
    }

    /** Devuelve el usuario si el correo (sin distinguir mayúsculas) y la contraseña coinciden; si no, null. */
    fun buscarPorCorreoYPassword(correo: String, password: String): Usuario? {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return usuarios.find {
                it.correo.equals(correo, ignoreCase = true) && it.password == password
            }
        }
        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT id, nombre, correo, password, rol FROM usuarios " +
                        "WHERE LOWER(correo) = LOWER(?) AND password = ?"
            ).use { ps ->
                ps.setString(1, correo)
                ps.setString(2, password)
                ps.executeQuery().use { rs ->
                    if (rs.next()) Usuario(
                        id = rs.getInt("id"),
                        nombre = rs.getString("nombre"),
                        correo = rs.getString("correo"),
                        password = rs.getString("password"),
                        rol = Rol.valueOf(rs.getString("rol"))
                    ) else null
                }
            }
        }
    }
}