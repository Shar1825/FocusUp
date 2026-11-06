package com.focusup.repository

import com.focusup.authentication.PasswordHasher
import com.focusup.database.DatabaseFactory
import com.focusup.model.Rol
import com.focusup.model.Usuario
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Repositorio de usuarios. Soporta tanto base de datos MySQL (si existen las variables de entorno)
 * como almacenamiento en memoria persistente inicializada desde usuarios.json.
 */
object UsuarioRepository {
    private val usuariosMap = ConcurrentHashMap<Int, Usuario>()
    private val nextId = AtomicInteger(100)

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
            val lista = json.decodeFromString<List<Usuario>>(jsonString)
            lista.forEach { u ->
                val id = u.id
                if (id >= nextId.get()) {
                    nextId.set(id + 1)
                }
                usuariosMap[id] = u
            }
        }.onFailure {
            println("Error al cargar usuarios.json: ${it.message}")
        }
    }

    fun buscarPorCorreoYPassword(correo: String, password: String): Usuario? {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            val u = usuariosMap.values.find {
                it.correo.equals(correo, ignoreCase = true)
            } ?: return null

            if (u.activo && PasswordHasher.verifyPassword(password, u.password)) {
                return u
            }
            return null
        }

        return ds.connection.use { c ->
            c.prepareStatement(
                "SELECT id, nombre, correo, password, rol, activo FROM usuarios " +
                        "WHERE LOWER(correo) = LOWER(?)"
            ).use { ps ->
                ps.setString(1, correo)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        val passStored = rs.getString("password")
                        val activo = rs.getBoolean("activo")
                        if (activo && PasswordHasher.verifyPassword(password, passStored)) {
                            Usuario(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                correo = rs.getString("correo"),
                                password = passStored,
                                rol = Rol.valueOf(rs.getString("rol")),
                                activo = activo
                            )
                        } else null
                    } else null
                }
            }
        }
    }

    fun buscarPorId(id: Int): Usuario? {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return usuariosMap[id]
        }
        return ds.connection.use { c ->
            c.prepareStatement("SELECT id, nombre, correo, password, rol, activo FROM usuarios WHERE id = ?").use { ps ->
                ps.setInt(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        Usuario(
                            id = rs.getInt(1),
                            nombre = rs.getString(2),
                            correo = rs.getString(3),
                            password = rs.getString(4),
                            rol = Rol.valueOf(rs.getString(5)),
                            activo = rs.getBoolean(6)
                        )
                    } else null
                }
            }
        }
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return usuariosMap.values.find { it.correo.equals(correo, ignoreCase = true) }
        }
        return ds.connection.use { c ->
            c.prepareStatement("SELECT id, nombre, correo, password, rol, activo FROM usuarios WHERE LOWER(correo) = LOWER(?)").use { ps ->
                ps.setString(1, correo)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        Usuario(
                            id = rs.getInt(1),
                            nombre = rs.getString(2),
                            correo = rs.getString(3),
                            password = rs.getString(4),
                            rol = Rol.valueOf(rs.getString(5)),
                            activo = rs.getBoolean(6)
                        )
                    } else null
                }
            }
        }
    }

    fun obtenerTodos(query: String? = null, rol: Rol? = null): List<Usuario> {
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            return usuariosMap.values.filter { u ->
                val matchQuery = query.isNullOrBlank() ||
                        u.nombre.contains(query, ignoreCase = true) ||
                        u.correo.contains(query, ignoreCase = true)
                val matchRol = rol == null || u.rol == rol || (rol == Rol.ADMINISTRADOR && u.rol == Rol.ADMIN)
                matchQuery && matchRol
            }.sortedBy { it.id }
        }

        return ds.connection.use { c ->
            var sql = "SELECT id, nombre, correo, password, rol, activo FROM usuarios WHERE 1=1"
            if (!query.isNullOrBlank()) sql += " AND (LOWER(nombre) LIKE ? OR LOWER(correo) LIKE ?)"
            if (rol != null) sql += " AND rol = ?"
            sql += " ORDER BY id ASC"

            c.prepareStatement(sql).use { ps ->
                var idx = 1
                if (!query.isNullOrBlank()) {
                    val p = "%${query.lowercase()}%"
                    ps.setString(idx++, p)
                    ps.setString(idx++, p)
                }
                if (rol != null) {
                    ps.setString(idx++, rol.name)
                }
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Usuario(
                                    id = rs.getInt(1),
                                    nombre = rs.getString(2),
                                    correo = rs.getString(3),
                                    password = rs.getString(4),
                                    rol = Rol.valueOf(rs.getString(5)),
                                    activo = rs.getBoolean(6)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun crearUsuario(nombre: String, correo: String, passwordPlana: String, rol: Rol): Usuario {
        if (buscarPorCorreo(correo) != null) {
            throw IllegalArgumentException("Ya existe un usuario registrado con el correo: $correo")
        }

        val passHash = PasswordHasher.hashPassword(passwordPlana)
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            val id = nextId.getAndIncrement()
            val nuevo = Usuario(id, nombre, correo, passHash, rol, activo = true)
            usuariosMap[id] = nuevo
            return nuevo
        }

        return ds.connection.use { c ->
            c.prepareStatement(
                "INSERT INTO usuarios (nombre, correo, password, rol, activo) VALUES (?, ?, ?, ?, true)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { ps ->
                ps.setString(1, nombre)
                ps.setString(2, correo)
                ps.setString(3, passHash)
                ps.setString(4, rol.name)
                ps.executeUpdate()
                ps.generatedKeys.use { rs ->
                    rs.next()
                    Usuario(rs.getInt(1), nombre, correo, passHash, rol, activo = true)
                }
            }
        }
    }

    fun actualizarPerfil(id: Int, nuevoNombre: String?, nuevoCorreo: String?): Usuario? {
        val actual = buscarPorId(id) ?: return null
        if (nuevoCorreo != null && !nuevoCorreo.equals(actual.correo, ignoreCase = true)) {
            val existente = buscarPorCorreo(nuevoCorreo)
            if (existente != null && existente.id != id) {
                throw IllegalArgumentException("El correo $nuevoCorreo ya está en uso por otro usuario.")
            }
        }

        val actualizado = actual.copy(
            nombre = nuevoNombre?.takeIf { it.isNotBlank() } ?: actual.nombre,
            correo = nuevoCorreo?.takeIf { it.isNotBlank() } ?: actual.correo
        )

        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            usuariosMap[id] = actualizado
            return actualizado
        }

        ds.connection.use { c ->
            c.prepareStatement("UPDATE usuarios SET nombre = ?, correo = ? WHERE id = ?").use { ps ->
                ps.setString(1, actualizado.nombre)
                ps.setString(2, actualizado.correo)
                ps.setInt(3, id)
                ps.executeUpdate()
            }
        }
        return actualizado
    }

    fun cambiarPassword(id: Int, actualPlana: String, nuevaPlana: String): Boolean {
        val u = buscarPorId(id) ?: return false
        if (!PasswordHasher.verifyPassword(actualPlana, u.password)) {
            return false
        }

        val nuevoHash = PasswordHasher.hashPassword(nuevaPlana)
        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            usuariosMap[id] = u.copy(password = nuevoHash)
            return true
        }

        ds.connection.use { c ->
            c.prepareStatement("UPDATE usuarios SET password = ? WHERE id = ?").use { ps ->
                ps.setString(1, nuevoHash)
                ps.setInt(2, id)
                ps.executeUpdate()
            }
        }
        return true
    }

    fun actualizarUsuarioPorAdmin(
        id: Int,
        nombre: String?,
        correo: String?,
        rol: Rol?,
        nuevaPasswordPlana: String?
    ): Usuario? {
        val actual = buscarPorId(id) ?: return null
        if (correo != null && !correo.equals(actual.correo, ignoreCase = true)) {
            val existente = buscarPorCorreo(correo)
            if (existente != null && existente.id != id) {
                throw IllegalArgumentException("El correo $correo ya está registrado por otro usuario.")
            }
        }

        val passFinal = if (!nuevaPasswordPlana.isNullOrBlank()) {
            PasswordHasher.hashPassword(nuevaPasswordPlana)
        } else actual.password

        val actualizado = actual.copy(
            nombre = nombre?.takeIf { it.isNotBlank() } ?: actual.nombre,
            correo = correo?.takeIf { it.isNotBlank() } ?: actual.correo,
            rol = rol ?: actual.rol,
            password = passFinal
        )

        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            usuariosMap[id] = actualizado
            return actualizado
        }

        ds.connection.use { c ->
            c.prepareStatement("UPDATE usuarios SET nombre = ?, correo = ?, rol = ?, password = ? WHERE id = ?").use { ps ->
                ps.setString(1, actualizado.nombre)
                ps.setString(2, actualizado.correo)
                ps.setString(3, actualizado.rol.name)
                ps.setString(4, actualizado.password)
                ps.setInt(5, id)
                ps.executeUpdate()
            }
        }
        return actualizado
    }

    fun cambiarEstado(id: Int, activo: Boolean): Usuario? {
        val u = buscarPorId(id) ?: return null
        val actualizado = u.copy(activo = activo)

        val ds = DatabaseFactory.dataSource
        if (ds == null) {
            usuariosMap[id] = actualizado
            return actualizado
        }

        ds.connection.use { c ->
            c.prepareStatement("UPDATE usuarios SET activo = ? WHERE id = ?").use { ps ->
                ps.setBoolean(1, activo)
                ps.setInt(2, id)
                ps.executeUpdate()
            }
        }
        return actualizado
    }
}
