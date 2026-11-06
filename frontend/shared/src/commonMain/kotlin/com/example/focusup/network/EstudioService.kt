package com.example.focusup.network

import com.example.focusup.model.EvidenciaEstado
import com.example.focusup.model.EvidenciaResumen
import com.example.focusup.model.SesionRequest
import com.example.focusup.model.SesionResponse
import com.example.focusup.model.ValidarRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Servicio de Red para Sesiones de Estudio y Evidencias (/api/sesiones, /api/evidencias)
 */
class EstudioService {

    private fun HttpRequestBuilder.agregarToken() {
        ApiConfig.tokenSesion?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend fun crearSesion(req: SesionRequest): Result<SesionResponse> = safeApiCall {
        ApiConfig.client.post("${ApiConfig.BASE_URL}/api/sesiones") {
            contentType(ContentType.Application.Json)
            setBody(req)
            agregarToken()
        }
    }

    suspend fun consultarEstadoEvidencia(id: Int): Result<EvidenciaEstado> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/evidencias/$id") {
            agregarToken()
        }
    }

    suspend fun subirEvidencia(sesionId: Int, imagenBytes: ByteArray): Result<EvidenciaEstado> = safeApiCall {
        ApiConfig.client.submitFormWithBinaryData(
            url = "${ApiConfig.BASE_URL}/api/evidencias",
            formData = formData {
                append("sesionId", sesionId.toString())
                append(
                    key = "imagen",
                    value = imagenBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"evidencia_$sesionId.jpg\"")
                    }
                )
            }
        ) {
            agregarToken()
        }
    }

    suspend fun getEvidenciasPendientes(): Result<List<EvidenciaResumen>> = safeApiCall {
        ApiConfig.client.get("${ApiConfig.BASE_URL}/api/evidencias/pendientes") { agregarToken() }
    }

    suspend fun validarEvidencia(id: Int, estado: String, comentario: String? = null): Result<EvidenciaEstado> = safeApiCall {
        ApiConfig.client.put("${ApiConfig.BASE_URL}/api/evidencias/$id") {
            contentType(ContentType.Application.Json)
            setBody(ValidarRequest(estado, comentario))
            agregarToken()
        }
    }

    private suspend inline fun <reified T> safeApiCall(block: () -> HttpResponse): Result<T> {
        return try {
            val response = block()
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                Result.success(response.body<T>())
            } else {
                Result.failure(Exception("Error HTTP ${response.status.value}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error al consultar API de estudio: ${e.message}"))
        }
    }
}
