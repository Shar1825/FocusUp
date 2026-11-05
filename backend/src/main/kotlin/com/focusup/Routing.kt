package com.focusup

import com.focusup.model.ErrorResponse
import com.focusup.model.LoginRequest
import com.focusup.model.LoginResponse
import com.focusup.repository.UsuarioRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Rutas del servidor: GET / (prueba de vida) y POST /api/login. */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor FocusUp Ktor funcionando correctamente!")
        }

        estudioRoutes()

        post("/api/login") {
            try {
                val request = call.receive<LoginRequest>()
                val usuario = UsuarioRepository.buscarPorCorreoYPassword(request.correo, request.password)

                if (usuario != null) {
                    // Se responde con LoginResponse: nunca se devuelve la contraseña
                    val response = LoginResponse(
                        id = usuario.id,
                        nombre = usuario.nombre,
                        correo = usuario.correo,
                        rol = usuario.rol
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Credenciales incorrectas. Verifique su correo y contraseña.")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Formato de solicitud inválido: ${e.message}")
                )
            }
        }
    }
}