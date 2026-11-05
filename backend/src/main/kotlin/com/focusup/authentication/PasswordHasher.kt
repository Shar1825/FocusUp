package com.focusup.authentication

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {
    private const val ALGORITHM = "SHA-256"

    /** Genera un hash SHA-256 con salt de la contraseña. Formato resultante: "salt:hashHex" */
    fun hashPassword(password: String): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        val salt = Base64.getEncoder().encodeToString(saltBytes)

        val digest = MessageDigest.getInstance(ALGORITHM)
        digest.update(saltBytes)
        val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        val hashHex = hashedBytes.joinToString("") { "%02x".format(it) }

        return "$salt:$hashHex"
    }

    /** Verfica si la contraseña ingresada coincide con el hash o texto plano almacenado. */
    fun verifyPassword(plainPassword: String, storedPassword: String): Boolean {
        if (!storedPassword.contains(":")) {
            // Comparación simple para retrocompatibilidad con contraseñas no desordenadas/planas
            return plainPassword == storedPassword
        }

        val parts = storedPassword.split(":")
        if (parts.size != 2) return plainPassword == storedPassword

        val salt = parts[0]
        val expectedHash = parts[1]

        val saltBytes = Base64.getDecoder().decode(salt)
        val digest = MessageDigest.getInstance(ALGORITHM)
        digest.update(saltBytes)
        val hashedBytes = digest.digest(plainPassword.toByteArray(Charsets.UTF_8))
        val actualHash = hashedBytes.joinToString("") { "%02x".format(it) }

        return actualHash.equals(expectedHash, ignoreCase = true)
    }
}
