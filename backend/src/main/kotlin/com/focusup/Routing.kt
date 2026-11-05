package com.focusup

import com.focusup.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Configuración global de rutas de la API FocusUp. */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor FocusUp Ktor funcionando correctamente!")
        }

        authRoutes()
        profileRoutes()
        adminRoutes()
        teacherRoutes()
        studentRoutes()
        estudioRoutes()
    }
}
