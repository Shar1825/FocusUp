package com.focusup

import com.focusup.model.ErrorResponse
import com.focusup.model.SesionRequest
import com.focusup.model.ValidarRequest
import com.focusup.repository.EstudioRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Rutas de sesiones de estudio y evidencias (cámara, validación docente). */
fun Route.estudioRoutes() {

    post("/api/sesiones") {
        try {
            val req = call.receive<SesionRequest>()
            val r = withContext(Dispatchers.IO) { EstudioRepository.crearSesion(req) }
            call.respond(HttpStatusCode.Created, r)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("No se pudo crear la sesión: ${e.message}"))
        }
    }

    // Subida de la foto: multipart con el campo "sesionId" y el archivo "imagen"
    post("/api/evidencias") {
        try {
            var sesionId: Int? = null
            var bytes: ByteArray? = null
            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "sesionId") sesionId = part.value.toIntOrNull()
                    is PartData.FileItem -> if (part.name == "imagen") bytes = part.provider().toByteArray()
                    else -> {}
                }
                part.dispose()
            }
            val sid = sesionId
            val img = bytes
            if (sid == null || img == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Faltan sesionId o imagen."))
                return@post
            }
            val r = withContext(Dispatchers.IO) { EstudioRepository.crearEvidencia(sid, img) }
            call.respond(HttpStatusCode.Created, r)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("No se pudo guardar la evidencia: ${e.message}"))
        }
    }

    get("/api/evidencias/pendientes") {
        try {
            call.respond(withContext(Dispatchers.IO) { EstudioRepository.pendientes() })
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error al consultar: ${e.message}"))
        }
    }

    get("/api/evidencias/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        val r = id?.let { withContext(Dispatchers.IO) { EstudioRepository.estado(it) } }
        if (r == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Evidencia no encontrada."))
        else call.respond(r)
    }

    get("/api/evidencias/{id}/imagen") {
        val id = call.parameters["id"]?.toIntOrNull()
        val img = id?.let { withContext(Dispatchers.IO) { EstudioRepository.imagen(it) } }
        if (img == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Imagen no encontrada."))
        else call.respondBytes(img, ContentType.Image.JPEG)
    }

    put("/api/evidencias/{id}") {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
            val req = call.receive<ValidarRequest>()
            if (id == null || req.estado !in setOf("APROBADA", "RECHAZADA")) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Estado debe ser APROBADA o RECHAZADA."))
                return@put
            }
            val ok = withContext(Dispatchers.IO) { EstudioRepository.validar(id, req.estado, req.comentario) }
            if (ok) call.respond(HttpStatusCode.OK, EstudioRepository.estado(id)!!)
            else call.respond(HttpStatusCode.NotFound, ErrorResponse("Evidencia no encontrada."))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Solicitud inválida: ${e.message}"))
        }
    }
}