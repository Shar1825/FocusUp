package com.focusup.routes

import com.focusup.dto.LoginResponseData
import com.focusup.model.ErrorResponse
import com.focusup.model.LoginRequest
import com.focusup.model.Rol
import com.focusup.services.AuthService
import com.focusup.utils.ApiSuccessResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CombinedLoginResponse(
    val success: Boolean = true,
    val message: String = "Inicio de sesión correcto",
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val user: LoginResponseData,
    val token: String
)

fun Route.authRoutes() {
    val handleLogin: suspend (ApplicationCall) -> Unit = { call ->
        try {
            val req = call.receive<LoginRequest>()
            if (req.correoEfectivo.isBlank() || req.password.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Formato de solicitud inválido: Correo y contraseña son requeridos.")
                )
            } else {
                val result = AuthService.login(req)
                if (result != null) {
                    val (authSuccess, legacyResponse) = result
                    val combined = CombinedLoginResponse(
                        success = true,
                        message = "Inicio de sesión correcto",
                        id = legacyResponse.id,
                        nombre = legacyResponse.nombre,
                        correo = legacyResponse.correo,
                        rol = legacyResponse.rol,
                        user = authSuccess.user,
                        token = authSuccess.token
                    )
                    call.respond(HttpStatusCode.OK, combined)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Credenciales incorrectas. Verifique su correo y contraseña.")
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Formato de solicitud inválido: ${e.message}")
            )
        }
    }

    val handleLogout: suspend (ApplicationCall) -> Unit = { call ->
        call.respond(
            HttpStatusCode.OK,
            ApiSuccessResponse(success = true, message = "Sesión cerrada correctamente.")
        )
    }

    post("/login") { handleLogin(call) }
    post("/api/login") { handleLogin(call) }

    post("/logout") { handleLogout(call) }
    post("/api/logout") { handleLogout(call) }
}
