package com.focusup.routes

import com.focusup.authentication.requireRole
import com.focusup.model.Rol
import com.focusup.model.StudentProfile
import com.focusup.services.ProfileService
import com.focusup.services.StudentService
import com.focusup.utils.ApiErrorResponse
import com.focusup.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studentRoutes() {

    val handleProfile: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val profile = ProfileService.getProfile(payload.userId) as? StudentProfile
            if (profile != null) {
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse(message = "Perfil de estudiante no encontrado."))
            }
        }
    }

    val handleGroups: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val grupos = StudentService.getGroups(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = grupos))
        }
    }

    val handleTeachers: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val docentes = StudentService.getTeachers(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = docentes))
        }
    }

    val handleActivities: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val actividades = StudentService.getActivities(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = actividades))
        }
    }

    val handleGrades: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val calificaciones = StudentService.getGrades(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = calificaciones))
        }
    }

    val handleProgress: suspend (ApplicationCall) -> Unit = { call ->
        call.requireRole(Rol.ESTUDIANTE) { payload ->
            val progreso = StudentService.getProgress(payload.userId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = progreso))
        }
    }

    get("/student/profile") { handleProfile(call) }
    get("/api/student/profile") { handleProfile(call) }

    get("/student/groups") { handleGroups(call) }
    get("/api/student/groups") { handleGroups(call) }

    get("/student/teachers") { handleTeachers(call) }
    get("/api/student/teachers") { handleTeachers(call) }

    get("/student/activities") { handleActivities(call) }
    get("/api/student/activities") { handleActivities(call) }

    get("/student/grades") { handleGrades(call) }
    get("/api/student/grades") { handleGrades(call) }

    get("/student/progress") { handleProgress(call) }
    get("/api/student/progress") { handleProgress(call) }
}
