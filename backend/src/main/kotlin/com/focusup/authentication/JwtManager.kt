package com.focusup.authentication

import com.focusup.model.Usuario
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Serializable
data class JwtPayload(
    val userId: Int,
    val email: String,
    val role: String,
    val exp: Long,
)

object JwtManager {
    private val SECRET_KEY = System.getenv("JWT_SECRET") ?: "focusup_jwt_secret_key_2026_safe_hash_token"
    private const val EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000L // 24 horas

    private val json = Json { ignoreUnknownKeys = true }

    fun generateToken(user: Usuario): String {
        val headerJson = """{"alg":"HS256","typ":"JWT"}"""
        val headerEncoded = urlSafeEncode(headerJson.toByteArray(Charsets.UTF_8))

        val payloadObj = JwtPayload(
            userId = user.id,
            email = user.correo,
            role = user.rol.name,
            exp = System.currentTimeMillis() + EXPIRATION_TIME_MS
        )
        val payloadJson = json.encodeToString(payloadObj)
        val payloadEncoded = urlSafeEncode(payloadJson.toByteArray(Charsets.UTF_8))

        val dataToSign = "$headerEncoded.$payloadEncoded"
        val signature = hmacSha256(dataToSign, SECRET_KEY)

        return "$dataToSign.$signature"
    }

    fun validateToken(token: String): JwtPayload? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val headerEncoded = parts[0]
        val payloadEncoded = parts[1]
        val signature = parts[2]

        val expectedSignature = hmacSha256("$headerEncoded.$payloadEncoded", SECRET_KEY)
        if (signature != expectedSignature) return null

        return try {
            val payloadBytes = urlSafeDecode(payloadEncoded)
            val payloadJson = String(payloadBytes, Charsets.UTF_8)
            val payload = json.decodeFromString<JwtPayload>(payloadJson)
            if (payload.exp < System.currentTimeMillis()) null else payload
        } catch (_: Exception) {
            null
        }
    }

    private fun hmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return urlSafeEncode(hmacBytes)
    }

    private fun urlSafeEncode(bytes: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun urlSafeDecode(str: String): ByteArray {
        return Base64.getUrlDecoder().decode(str)
    }
}

/** Extrae el token Bearer de la cabecera Authorization y retorna el payload decodificado si es válido. */
fun ApplicationCall.getAuthenticatedUser(): JwtPayload? {
    val authHeader = request.header("Authorization") ?: return null
    if (!authHeader.startsWith("Bearer ", ignoreCase = true)) return null
    val token = authHeader.substring(7).trim()
    return JwtManager.validateToken(token)
}
