package com.example.focusup.network

import com.example.focusup.model.Activity
import com.example.focusup.model.ApiResponse
import com.example.focusup.model.CreateActivityRequest
import com.example.focusup.model.Grade
import com.example.focusup.model.Group
import com.example.focusup.model.RegisterGradeRequest
import com.example.focusup.model.TeacherProfile
import com.example.focusup.model.UserDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Servicio de Red para Docentes: llama a los endpoints de /api/teacher
 */
class TeacherService {

    private fun HttpRequestBuilder.agregarToken() {
        ApiConfig.tokenSesion?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend fun getProfile(): Result<TeacherProfile> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/teacher/profile") { agregarToken() }
    }

    suspend fun getGroups(): Result<List<Group>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/teacher/groups") { agregarToken() }
    }

    suspend fun getStudents(): Result<List<UserDto>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/teacher/students") { agregarToken() }
    }

    suspend fun getActivities(): Result<List<Activity>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/teacher/activities") { agregarToken() }
    }

    suspend fun createActivity(req: CreateActivityRequest): Result<Activity> = safeApiCall {
        ApiConfig.client.post("${ApiConfig.BASE_URL}/api/teacher/activities") {
            contentType(ContentType.Application.Json)
            setBody(req)
            agregarToken()
        }
    }

    suspend fun getGrades(): Result<List<Grade>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/teacher/grades") { agregarToken() }
    }

    suspend fun registerGrade(req: RegisterGradeRequest): Result<Grade> = safeApiCall {
        ApiConfig.client.post("${ApiConfig.BASE_URL}/api/teacher/grades") {
            contentType(ContentType.Application.Json)
            setBody(req)
            agregarToken()
        }
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
