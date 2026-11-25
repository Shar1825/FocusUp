package com.focusup

import com.focusup.database.DatabaseInitializer
import com.focusup.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Configuración global de rutas de la API FocusUp. */
fun Application.configureRouting() {

    // Inicializa y valida el esquema MySQL y registra el estado en el log al arrancar el servidor
    DatabaseInitializer.initSchema()

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
