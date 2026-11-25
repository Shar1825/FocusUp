package com.focusup

import com.focusup.dto.AdminStatisticsResponse
import com.focusup.dto.AuthSuccessResponse
import com.focusup.model.*
import com.focusup.repository.EstudioRepository
import com.focusup.utils.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Pruebas de integración para las métricas reales de Estudiante, Docente y Administrador.
 */
class MetricasRealesTest {

    private val jsonConfig = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private fun ApplicationTestBuilder.configurar(): HttpClient {
        environment { config = MapApplicationConfig() }
        application {
            install(ServerContentNegotiation) { json(jsonConfig) }
            configureRouting()
        }
        return createClient {
            install(ClientContentNegotiation) { json(jsonConfig) }
        }
    }

    private suspend fun HttpClient.login(correo: String, pass: String): String {
        val res = post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(correo, pass))
        }
        assertEquals(HttpStatusCode.OK, res.status)
        val body = res.body<AuthSuccessResponse>()
        assertTrue(body.token.isNotBlank())
        return body.token
    }

    @Test
    fun testMetricasProgresoEstudiante() = testApplication {
        val client = configurar()
        val token = client.login("estudiante@focusup.com", "estudiante123")

        // 1. Crear una sesión de estudio de 30 minutos para el estudiante (id=3)
        val s = EstudioRepository.crearSesion(SesionRequest(estudianteId = 3, objetivo = "Estudiar Ktor", duracionMin = 30))
        val e = EstudioRepository.crearEvidencia(s.id, byteArrayOf(1, 2, 3))
        EstudioRepository.validar(e.id, "APROBADA", "Excelente evidencia")

        // 2. Consultar GET /api/student/progress
        val response = client.get("/api/student/progress") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<ApiResponse<AcademicProgress>>()
        assertTrue(body.success)
        val data = body.data
        assertNotNull(data)

        assertEquals(3, data.estudianteId)
        assertTrue(data.actividadesTotales >= 0)
        assertTrue(data.actividadesCompletadas >= 0)
        assertTrue(data.actividadesEntregadas >= 0)
        assertTrue(data.actividadesPendientes >= 0)
        assertTrue(data.promedioCalificaciones >= 0.0)
        assertTrue(data.minutosEstudioHoy >= 30)
        assertTrue(data.rachaDias >= 1)
    }

    @Test
    fun testMetricasPerfilDocente() = testApplication {
        val client = configurar()
        val token = client.login("docente@focusup.com", "docente123")

        // Consultar GET /api/teacher/profile
        val response = client.get("/api/teacher/profile") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<ApiResponse<TeacherProfile>>()
        assertTrue(body.success)
        val data = body.data
        assertNotNull(data)

        assertEquals(Rol.DOCENTE, data.rol)
        assertTrue(data.gruposAsignadosCount >= 0)
        assertTrue(data.totalEstudiantesCount >= 0)
        assertTrue(data.entregasPendientesCalificar >= 0)
        assertTrue(data.evidenciasPendientes >= 0)
    }

    @Test
    fun testMetricasEstadisticasAdmin() = testApplication {
        val client = configurar()
        val token = client.login("admin@focusup.com", "admin123")

        // Consultar GET /api/admin/statistics
        val response = client.get("/api/admin/statistics") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<ApiResponse<AdminStatisticsResponse>>()
        assertTrue(body.success)
        val stats = body.data
        assertNotNull(stats)

        assertTrue(stats.totalUsuarios >= 3)
        assertTrue(stats.usuariosActivos >= 1)
        assertTrue(stats.usuariosActivosAdmin >= 1)
        assertTrue(stats.usuariosActivosDocente >= 1)
        assertTrue(stats.usuariosActivosEstudiante >= 1)
        assertTrue(stats.totalGrupos >= 0)
        assertTrue(stats.totalActividades >= 0)
        assertTrue(stats.totalSesiones >= 0)
        assertTrue(stats.evidenciasPendientes >= 0)
        assertTrue(stats.evidenciasAprobadas >= 0)
        assertTrue(stats.evidenciasRechazadas >= 0)
    }

    @Test
    fun testPermisosRolesEnEndpointsDeMetricas() = testApplication {
        val client = configurar()
        val estudianteToken = client.login("estudiante@focusup.com", "estudiante123")
        val docenteToken = client.login("docente@focusup.com", "docente123")

        // Estudiante intentando acceder a estadísticas admin -> 403 Forbidden
        val resAdmin = client.get("/api/admin/statistics") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.Forbidden, resAdmin.status)

        // Estudiante intentando acceder a perfil docente -> 403 Forbidden
        val resDocente = client.get("/api/teacher/profile") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.Forbidden, resDocente.status)

        // Docente intentando acceder a progreso estudiante -> 403 Forbidden
        val resEstudiante = client.get("/api/student/progress") {
            header("Authorization", "Bearer $docenteToken")
        }
        assertEquals(HttpStatusCode.Forbidden, resEstudiante.status)
    }
}
