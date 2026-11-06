package com.focusup.routes

import com.focusup.authentication.requireRole
import com.focusup.dto.CreateUserRequest
import com.focusup.dto.UpdateUserRequest
import com.focusup.dto.UpdateUserStatusRequest
import com.focusup.model.Rol
import com.focusup.services.AdminService
import com.focusup.utils.ApiErrorResponse
import com.focusup.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {

    val handleGetUsers: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            val query = call.request.queryParameters["query"]
            val rolParam = call.request.queryParameters["rol"]?.let { r ->
                runCatching { Rol.valueOf(r.uppercase()) }.getOrNull()
            }
            val users = AdminService.getUsers(query, rolParam)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = users))
        }
    }

    val handleCreateUser: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            try {
                val req = call.receive<CreateUserRequest>()
                if (req.nombre.isBlank() || req.correo.isBlank() || req.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Nombre, correo y contraseña son requeridos."))
                    return@requireRole
                }
                val nuevo = AdminService.createUser(req)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Usuario registrado exitosamente.", data = nuevo))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al crear usuario: ${e.message}"))
            }
        }
    }

    val handleGetUserById: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de usuario inválido."))
                return@requireRole
            }
            val u = AdminService.getUserById(id)
            if (u != null) {
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = u))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Usuario no encontrado."))
            }
        }
    }

    val handleUpdateUser: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de usuario inválido."))
                return@requireRole
            }
            try {
                val req = call.receive<UpdateUserRequest>()
                val act = AdminService.updateUser(id, req)
                if (act != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Usuario actualizado correctamente.", data = act))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Usuario no encontrado."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al actualizar usuario: ${e.message}"))
            }
        }
    }

    val handleUpdateStatus: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de usuario inválido."))
                return@requireRole
            }
            try {
                val req = call.receive<UpdateUserStatusRequest>()
                val act = AdminService.updateUserStatus(id, req)
                if (act != null) {
                    val statusText = if (act.activo) "activado" else "desactivado"
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Estado de usuario $statusText correctamente.", data = act))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Usuario no encontrado."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al cambiar estado: ${e.message}"))
            }
        }
    }

    val handleGetStatistics: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ADMINISTRADOR) {
            val stats = AdminService.getStatistics()
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = stats))
        }
    }

    get("/admin/users") { handleGetUsers(call) }
    get("/api/admin/users") { handleGetUsers(call) }

    post("/admin/users") { handleCreateUser(call) }
    post("/api/admin/users") { handleCreateUser(call) }

    get("/admin/users/{id}") { handleGetUserById(call) }
    get("/api/admin/users/{id}") { handleGetUserById(call) }

    put("/admin/users/{id}") { handleUpdateUser(call) }
    put("/api/admin/users/{id}") { handleUpdateUser(call) }

    patch("/admin/users/{id}/status") { handleUpdateStatus(call) }
    patch("/api/admin/users/{id}/status") { handleUpdateStatus(call) }

    get("/admin/statistics") { handleGetStatistics(call) }
    get("/api/admin/statistics") { handleGetStatistics(call) }
}
