package com.example.focusup.network

import com.example.focusup.model.AdminStatistics
import com.example.focusup.model.ApiResponse
import com.example.focusup.model.CreateUserRequest
import com.example.focusup.model.Rol
import com.example.focusup.model.UpdateUserStatusRequest
import com.example.focusup.model.UserDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Servicio de Red para Administradores: llama a los endpoints de /api/admin
 */
class AdminService {

    private fun HttpRequestBuilder.agregarToken() {
        ApiConfig.tokenSesion?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend fun getUsers(query: String? = null, rol: Rol? = null): Result<List<UserDto>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/admin/users") {
            query?.let { parameter("query", it) }
            rol?.let { parameter("rol", it.name) }
            agregarToken()
        }
    }

    suspend fun createUser(req: CreateUserRequest): Result<UserDto> = safeApiCall {
        ApiConfig.client.post("${ApiConfig.BASE_URL}/api/admin/users") {
            contentType(ContentType.Application.Json)
            setBody(req)
            agregarToken()
        }
    }

    suspend fun updateUserStatus(id: Int, activo: Boolean): Result<UserDto> = safeApiCall {
        ApiConfig.client.patch("${ApiConfig.BASE_URL}/api/admin/users/$id/status") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserStatusRequest(activo))
            agregarToken()
        }
    }

    suspend fun getStatistics(): Result<AdminStatistics> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/admin/statistics") { agregarToken() }
    }

    private suspend inline fun <reified T> safeApiCall(block: () -> HttpResponse): Result<T> {
        return try {
            val response = block()
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val apiResp = runCatching { response.body<ApiResponse<T>>() }.getOrNull()
                if (apiResp?.data != null) {
                    Result.success(apiResp.data)
                } else {
                    val direct = runCatching { response.body<T>() }.getOrNull()
                    if (direct != null) Result.success(direct)
                    else Result.failure(Exception(apiResp?.message ?: "Sin datos"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.status.value}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error al consultar API: ${e.message}"))
        }
    }
}
