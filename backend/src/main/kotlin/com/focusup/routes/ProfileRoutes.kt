package com.focusup.routes

import com.focusup.authentication.requireAuthentication
import com.focusup.dto.ChangePasswordRequest
import com.focusup.dto.UpdateProfileRequest
import com.focusup.model.AdminProfile
import com.focusup.model.StudentProfile
import com.focusup.model.TeacherProfile
import com.focusup.services.ProfileService
import com.focusup.utils.ApiErrorResponse
import com.focusup.utils.ApiResponse
import com.focusup.utils.ApiSuccessResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.profileRoutes() {
    val handleGetProfile: suspend (ApplicationCall) -> Unit = { call ->
        call.requireAuthentication { payload ->
            val profile = ProfileService.getProfile(payload.userId)
            when (profile) {
                is AdminProfile -> call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
                is TeacherProfile -> call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
                is StudentProfile -> call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
                else -> call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Perfil no encontrado."))
            }
        }
    }

    val handleUpdateProfile: suspend (ApplicationCall) -> Unit = { call ->
        call.requireAuthentication { payload ->
            try {
                val req = call.receive<UpdateProfileRequest>()
                val updated = ProfileService.updateProfile(payload.userId, req)
                if (updated != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Perfil actualizado con éxito", data = updated))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "No se pudo actualizar el perfil."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error en los datos suministrados: ${e.message}"))
            }
        }
    }

    val handleChangePassword: suspend (ApplicationCall) -> Unit = { call ->
        call.requireAuthentication { payload ->
            try {
                val req = call.receive<ChangePasswordRequest>()
                if (req.nuevaPassword.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "La nueva contraseña no puede estar vacía."))
                    return@requireAuthentication
                }
                val ok = ProfileService.changePassword(payload.userId, req)
                if (ok) {
                    call.respond(HttpStatusCode.OK, ApiSuccessResponse(success = true, message = "Contraseña cambiada exitosamente."))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "La contraseña actual es incorrecta."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error en los datos de contraseña: ${e.message}"))
            }
        }
    }

    get("/profile") { handleGetProfile(call) }
    get("/api/profile") { handleGetProfile(call) }

    put("/profile") { handleUpdateProfile(call) }
    put("/api/profile") { handleUpdateProfile(call) }

    put("/profile/password") { handleChangePassword(call) }
    put("/api/profile/password") { handleChangePassword(call) }
}
