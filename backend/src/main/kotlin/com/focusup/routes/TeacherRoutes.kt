package com.focusup.routes

import com.focusup.authentication.requireRole
import com.focusup.dto.CreateActivityRequest
import com.focusup.dto.RegisterGradeRequest
import com.focusup.dto.UpdateActivityRequest
import com.focusup.model.Rol
import com.focusup.model.TeacherProfile
import com.focusup.services.ProfileService
import com.focusup.services.TeacherService
import com.focusup.utils.ApiErrorResponse
import com.focusup.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.teacherRoutes() {

    val handleProfile: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val profile = ProfileService.getProfile(payload.userId) as? TeacherProfile
            if (profile != null) {
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Perfil de docente no encontrado."))
            }
        }
    }

    val handleGroups: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val grupos = TeacherService.getGroups(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = grupos))
        }
    }

    val handleStudents: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val estudiantes = TeacherService.getStudents(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = estudiantes))
        }
    }

    val handleGetActivities: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val actividades = TeacherService.getActivities(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = actividades))
        }
    }

    val handleCreateActivity: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            try {
                val req = call.receive<CreateActivityRequest>()
                if (req.titulo.isBlank() || req.descripcion.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Título y descripción son obligatorios."))
                    return@requireRole
                }
                val nueva = TeacherService.createActivity(payload.userId, req)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Actividad creada con éxito.", data = nueva))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al crear actividad: ${e.message}"))
            }
        }
    }

    val handleUpdateActivity: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de actividad inválido."))
                return@requireRole
            }
            try {
                val req = call.receive<UpdateActivityRequest>()
                val actualizada = TeacherService.updateActivity(payload.userId, id, req)
                if (actualizada != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Actividad actualizada.", data = actualizada))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Actividad no encontrada."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al actualizar actividad: ${e.message}"))
            }
        }
    }

    val handleGetGrades: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val calificaciones = TeacherService.getGrades(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = calificaciones))
        }
    }

    val handleRegisterGrade: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            try {
                val req = call.receive<RegisterGradeRequest>()
                val cal = TeacherService.registerGrade(payload.userId, req)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Calificación registrada exitosamente.", data = cal))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Error al registrar calificación: ${e.message}"))
            }
        }
    }

    val handleStudentProgress: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.DOCENTE) { payload ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de estudiante inválido."))
                return@requireRole
            }
            try {
                val progreso = TeacherService.getStudentProgress(payload.userId, id)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = progreso))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = e.message ?: "Error al consultar progreso."))
            }
        }
    }

    get("/teacher/profile") { handleProfile(call) }
    get("/api/teacher/profile") { handleProfile(call) }

    get("/teacher/groups") { handleGroups(call) }
    get("/api/teacher/groups") { handleGroups(call) }

    get("/teacher/students") { handleStudents(call) }
    get("/api/teacher/students") { handleStudents(call) }

    get("/teacher/activities") { handleGetActivities(call) }
    get("/api/teacher/activities") { handleGetActivities(call) }

    post("/teacher/activities") { handleCreateActivity(call) }
    post("/api/teacher/activities") { handleCreateActivity(call) }

    put("/teacher/activities/{id}") { handleUpdateActivity(call) }
    put("/api/teacher/activities/{id}") { handleUpdateActivity(call) }

    get("/teacher/grades") { handleGetGrades(call) }
    get("/api/teacher/grades") { handleGetGrades(call) }

    post("/teacher/grades") { handleRegisterGrade(call) }
    post("/api/teacher/grades") { handleRegisterGrade(call) }

    get("/teacher/students/{id}/progress") { handleStudentProgress(call) }
    get("/api/teacher/students/{id}/progress") { handleStudentProgress(call) }
}
