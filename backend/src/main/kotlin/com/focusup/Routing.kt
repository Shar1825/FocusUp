package com.focusup

import com.focusup.database.DatabaseInitializer
import com.focusup.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Configuración de CORS para acceso web remoto y local del Administrador. */
fun Application.configureCORS() {
    install(CORS) {
        // Desarrollo local
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("127.0.0.1:8080", schemes = listOf("http"))

        // Host remotos desde variable de entorno CORS_ALLOWED_HOSTS (separados por comas)
        val extraHosts = System.getenv("CORS_ALLOWED_HOSTS")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        for (h in extraHosts) {
            val hostClean = h.removePrefix("http://").removePrefix("https://").trimEnd('/')
            if (hostClean.isNotBlank()) {
                allowHost(hostClean, schemes = listOf("https", "http"))
            }
        }

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}

/** Configuración global de rutas e inicialización de la API FocusUp. */
fun Application.configureRouting() {

    // Configurar CORS para peticiones web antes de routing
    configureCORS()

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
