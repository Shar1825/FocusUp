package com.focusup.authentication

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest
import java.util.Base64

object PasswordHasher {

    /** Genera un hash BCrypt seguro de la contraseña. El resultado comienza con "$2a$" o "$2b$" */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray())
    }

    /**
     * Verifica si la contraseña ingresada en texto plano coincide con la contraseña almacenada.
     * Soporta hashes BCrypt ($2...), SHA-256 legacy (salt:hash) y texto plano.
     */
    fun verifyPassword(plainPassword: String, storedPassword: String): Boolean {
        if (storedPassword.isBlank()) return false

        // 1. Si la contraseña almacenada empieza con $2, es un hash BCrypt
        if (storedPassword.startsWith("$2")) {
            return runCatching {
                BCrypt.verifyer().verify(plainPassword.toCharArray(), storedPassword).verified
            }.getOrDefault(false)
        }

        // 2. Si contiene un delimitador ':', intentar verificación con formato SHA-256 legacy
        if (storedPassword.contains(":")) {
            val parts = storedPassword.split(":")
            if (parts.size == 2) {
                runCatching {
                    val salt = parts[0]
                    val expectedHash = parts[1]
                    val saltBytes = Base64.getDecoder().decode(salt)
                    val digest = MessageDigest.getInstance("SHA-256")
                    digest.update(saltBytes)
                    val hashedBytes = digest.digest(plainPassword.toByteArray(Charsets.UTF_8))
                    val actualHash = hashedBytes.joinToString("") { "%02x".format(it) }
                    if (actualHash.equals(expectedHash, ignoreCase = true)) return true
                }
            }
        }

        // 3. Fallback: Comparación directa en texto plano (retrocompatibilidad)
        return plainPassword == storedPassword
    }
}
