package com.example.focusup.network

import com.example.focusup.model.ErrorResponse
import com.example.focusup.model.LoginRequest
import com.example.focusup.model.LoginResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Servicio de Autenticación: se conecta a los endpoints del backend `/api/login` y `/api/logout`.
 */
class AuthService {

    /** POST /api/login */
    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = ApiConfig.client.post("${ApiConfig.BASE_URL}/api/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(correo, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val loginResponse = response.body<LoginResponse>()
                ApiConfig.tokenSesion = loginResponse.token
                Result.success(loginResponse)
            } else {
                val mensaje = runCatching { response.body<ErrorResponse>().mensaje }.getOrNull()
                Result.failure(Exception(mensaje ?: "Credenciales incorrectas. Verifique su correo y contraseña."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión con el servidor: ${e.message}"))
        }
    }

    /** POST /api/logout */
    suspend fun logout(): Result<Boolean> {
        return try {
            val response = ApiConfig.client.post("${ApiConfig.BASE_URL}/api/logout") {
                contentType(ContentType.Application.Json)
                ApiConfig.tokenSesion?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }
            ApiConfig.tokenSesion = null
            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiConfig.tokenSesion = null
            Result.success(true)
        }
    }
}
