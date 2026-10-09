package com.focusup

import com.focusup.model.ErrorResponse
import com.focusup.model.LoginRequest
import com.focusup.model.LoginResponse
import com.focusup.model.Rol
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pruebas de la ruta POST /api/login (usuarios de usuarios.json).
 * Cubre: login correcto por rol, credenciales inválidas, correo en mayúsculas,
 * JSON mal formado, campos faltantes y que nunca se devuelva la contraseña.
 */
class LoginRoutesTest {

    /**
     * Monta el servidor de pruebas con serialización JSON y las rutas reales; devuelve un cliente.
     * Se vacía la configuración (application.yaml) para que el módulo del servidor real no se
     * cargue por duplicado y los plugins no se instalen dos veces.
     */
    private fun ApplicationTestBuilder.configurar(): HttpClient {
        environment { config = MapApplicationConfig() }
        application {
            install(ServerContentNegotiation) { json() }
            configureRouting()
        }
        return createClient { install(ClientContentNegotiation) { json() } }
    }

    private suspend fun HttpClient.login(correo: String, password: String) =
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(correo, password))
        }

    @Test
    fun rutaRaizResponde() = testApplication {
        val client = configurar()
        val respuesta = client.get("/")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        assertTrue(respuesta.bodyAsText().contains("FocusUp"))
    }

    @Test
    fun loginAdministradorDevuelve200ConRolAdministrador() = testApplication {
        val client = configurar()
        val respuesta = client.login("admin@focusup.com", "admin123")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        assertEquals(Rol.ADMINISTRADOR, respuesta.body<LoginResponse>().rol)
    }

    @Test
    fun loginDocenteDevuelve200ConRolDocente() = testApplication {
        val client = configurar()
        val respuesta = client.login("docente@focusup.com", "docente123")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        assertEquals(Rol.DOCENTE, respuesta.body<LoginResponse>().rol)
    }

    @Test
    fun loginEstudianteDevuelve200ConRolEstudiante() = testApplication {
        val client = configurar()
        val respuesta = client.login("estudiante@focusup.com", "estudiante123")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        assertEquals(Rol.ESTUDIANTE, respuesta.body<LoginResponse>().rol)
    }

    @Test
    fun correoNoDistingueMayusculas() = testApplication {
        val client = configurar()
        val respuesta = client.login("ADMIN@FOCUSUP.COM", "admin123")

        assertEquals(HttpStatusCode.OK, respuesta.status)
    }

    @Test
    fun passwordIncorrectaDevuelve401ConMensaje() = testApplication {
        val client = configurar()
        val respuesta = client.login("admin@focusup.com", "incorrecta")

        assertEquals(HttpStatusCode.Unauthorized, respuesta.status)
        assertTrue(respuesta.body<ErrorResponse>().mensaje.contains("incorrectas"))
    }

    @Test
    fun correoInexistenteDevuelve401() = testApplication {
        val client = configurar()
        val respuesta = client.login("nadie@focusup.com", "admin123")

        assertEquals(HttpStatusCode.Unauthorized, respuesta.status)
    }

    @Test
    fun jsonMalFormadoDevuelve400() = testApplication {
        val client = configurar()
        val respuesta = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody("{esto no es json")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
    }

    @Test
    fun camposFaltantesDevuelve400() = testApplication {
        val client = configurar()
        val respuesta = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"correo":"admin@focusup.com"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
    }

    @Test
    fun laRespuestaNuncaIncluyeLaPassword() = testApplication {
        val client = configurar()
        val texto = client.login("admin@focusup.com", "admin123").bodyAsText()

        assertFalse(texto.contains("password"))
        assertFalse(texto.contains("admin123"))
    }
}