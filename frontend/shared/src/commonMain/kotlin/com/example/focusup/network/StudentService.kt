package com.example.focusup.network

import com.example.focusup.model.AcademicProgress
import com.example.focusup.model.Activity
import com.example.focusup.model.ApiResponse
import com.example.focusup.model.Grade
import com.example.focusup.model.Group
import com.example.focusup.model.StudentProfile
import com.example.focusup.model.TeacherInfo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Servicio de Red para Estudiantes: llama a los endpoints de /api/student
 */
class StudentService {

    private fun HttpRequestBuilder.agregarToken() {
        ApiConfig.tokenSesion?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend fun getProfile(): Result<StudentProfile> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/profile") { agregarToken() }
    }

    suspend fun getGroups(): Result<List<Group>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/groups") { agregarToken() }
    }

    suspend fun getTeachers(): Result<List<TeacherInfo>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/teachers") { agregarToken() }
    }

    suspend fun getActivities(): Result<List<Activity>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/activities") { agregarToken() }
    }

    suspend fun getGrades(): Result<List<Grade>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/grades") { agregarToken() }
    }

    suspend fun getProgress(): Result<AcademicProgress> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/student/progress") { agregarToken() }
    }

    private suspend inline fun <reified T> safeApiCall(block: () -> HttpResponse): Result<T> {
        return try {
            val response = block()
            if (response.status == HttpStatusCode.OK) {
                val apiResp = runCatching { response.body<ApiResponse<T>>() }.getOrNull()
                if (apiResp?.data != null) {
                    Result.success(apiResp.data)
                } else {
                    val direct = runCatching { response.body<T>() }.getOrNull()
                    if (direct != null) Result.success(direct)
                    else Result.failure(Exception(apiResp?.message ?: "Sin datos devueltos"))
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
