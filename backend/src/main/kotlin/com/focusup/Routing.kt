package com.focusup

import com.focusup.database.DatabaseInitializer
import com.focusup.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


/** Configuración de CORS para acceso web del Administrador y clientes. */
fun Application.configureCORS() {
    install(CORS) {
        // Desarrollo local
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("127.0.0.1:8080", schemes = listOf("http"))
        // Producción: agrega aquí el dominio de tu web cuando la despliegues
        // allowHost("tu-dominio.up.railway.app", schemes = listOf("https"))

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

    // Configurar CORS para peticiones web
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
