package com.example.focusup.network

import com.example.focusup.model.ErrorResponse
import com.example.focusup.model.LoginRequest
import com.example.focusup.model.LoginResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Servicio de red: único lugar que conoce Ktor Client y las rutas del backend.
 * Convierte la respuesta HTTP en un [Result] para que las capas superiores no manejen HTTP.
 */
class AuthService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /** POST /api/login. Éxito -> [LoginResponse]; error -> mensaje que envió el servidor. */
    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = client.post("${ApiConfig.BASE_URL}/api/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(correo, password))
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<LoginResponse>())
            } else {
                // Usa el ErrorResponse del backend; si no se puede leer, mensaje genérico
                val mensaje = runCatching { response.body<ErrorResponse>().mensaje }.getOrNull()
                Result.failure(Exception(mensaje ?: "Credenciales inválidas"))
            }
        } catch (e: CancellationException) {
            throw e // nunca se "traga" una cancelación de corrutina
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}