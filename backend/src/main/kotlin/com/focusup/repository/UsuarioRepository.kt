package com.focusup.repository

import com.focusup.model.Usuario
import kotlinx.serialization.json.Json

/**
 * Repositorio de usuarios. Carga una sola vez las credenciales predefinidas desde
 * src/main/resources/usuarios.json y permite buscarlas por correo y contraseña.
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
        return usuarios.find {
            it.correo.equals(correo, ignoreCase = true) && it.password == password
        }
    }
}