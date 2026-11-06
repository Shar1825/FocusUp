package com.focusup

import com.focusup.dto.*
import com.focusup.model.*
import com.focusup.utils.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
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

class PerfilYRolesTest {

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
    fun testLoginYConsultaPerfilTodosLosRoles() = testApplication {
        val client = configurar()

        val adminToken = client.login("admin@focusup.com", "admin123")
        val docenteToken = client.login("docente@focusup.com", "docente123")
        val estudianteToken = client.login("estudiante@focusup.com", "estudiante123")

        // Profile Admin
        val adminProfileRes = client.get("/profile") {
            header("Authorization", "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, adminProfileRes.status)
        val adminBody = adminProfileRes.body<ApiResponse<AdminProfile>>()
        assertEquals(Rol.ADMINISTRADOR, adminBody.data?.rol)

        // Profile Teacher
        val teacherProfileRes = client.get("/teacher/profile") {
            header("Authorization", "Bearer $docenteToken")
        }
        assertEquals(HttpStatusCode.OK, teacherProfileRes.status)
        val teacherBody = teacherProfileRes.body<ApiResponse<TeacherProfile>>()
        assertEquals(Rol.DOCENTE, teacherBody.data?.rol)

        // Profile Student
        val studentProfileRes = client.get("/student/profile") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, studentProfileRes.status)
        val studentBody = studentProfileRes.body<ApiResponse<StudentProfile>>()
        assertEquals(Rol.ESTUDIANTE, studentBody.data?.rol)
    }

    @Test
    fun testEdicionPerfilYCambioPassword() = testApplication {
        val client = configurar()
        val token = client.login("admin@focusup.com", "admin123")

        // Update profile name
        val updateRes = client.put("/profile") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(nombre = "Admin Nombre Editado"))
        }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updateBody = updateRes.body<ApiResponse<UserDto>>()
        assertEquals("Admin Nombre Editado", updateBody.data?.nombre)

        // Change password
        val passRes = client.put("/profile/password") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(passwordActual = "admin123", nuevaPassword = "adminNuevaPassword123"))
        }
        assertEquals(HttpStatusCode.OK, passRes.status)

        // Login with new password
        val newToken = client.login("admin@focusup.com", "adminNuevaPassword123")
        assertTrue(newToken.isNotBlank())

        // Restore original password for other tests
        client.put("/profile/password") {
            header("Authorization", "Bearer $newToken")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(passwordActual = "adminNuevaPassword123", nuevaPassword = "admin123"))
        }
    }

    @Test
    fun testAdminGestionUsuariosYEstadisticas() = testApplication {
        val client = configurar()
        val adminToken = client.login("admin@focusup.com", "admin123")

        // Register new user (Docente)
        val newDocenteRes = client.post("/admin/users") {
            header("Authorization", "Bearer $adminToken")
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(nombre = "Nuevo Docente Test", correo = "nuevo.docente@focusup.com", password = "pass123", rol = Rol.DOCENTE))
        }
        assertEquals(HttpStatusCode.Created, newDocenteRes.status)
        val nuevoDocente = newDocenteRes.body<ApiResponse<UserDto>>().data
        assertNotNull(nuevoDocente)

        // List all users
        val listRes = client.get("/admin/users") {
            header("Authorization", "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, listRes.status)
        val listBody = listRes.body<ApiResponse<List<UserDto>>>()
        assertTrue((listBody.data?.size ?: 0) >= 4)

        // Deactivate user
        val statusRes = client.patch("/admin/users/${nuevoDocente.id}/status") {
            header("Authorization", "Bearer $adminToken")
            contentType(ContentType.Application.Json)
            setBody(UpdateUserStatusRequest(activo = false))
        }
        assertEquals(HttpStatusCode.OK, statusRes.status)
        assertEquals(false, statusRes.body<ApiResponse<UserDto>>().data?.activo)

        // Statistics
        val statsRes = client.get("/admin/statistics") {
            header("Authorization", "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, statsRes.status)
        val stats = statsRes.body<ApiResponse<AdminStatisticsResponse>>().data
        assertNotNull(stats)
        assertTrue(stats.totalUsuarios >= 4)
    }

    @Test
    fun testDocenteGestionActividadesYCalificaciones() = testApplication {
        val client = configurar()
        val docenteToken = client.login("docente@focusup.com", "docente123")

        // Get groups
        val groupsRes = client.get("/teacher/groups") {
            header("Authorization", "Bearer $docenteToken")
        }
        assertEquals(HttpStatusCode.OK, groupsRes.status)
        val grupos = groupsRes.body<ApiResponse<List<Group>>>().data
        assertTrue(!grupos.isNullOrEmpty())

        // Create Activity
        val createActRes = client.post("/teacher/activities") {
            header("Authorization", "Bearer $docenteToken")
            contentType(ContentType.Application.Json)
            setBody(CreateActivityRequest(grupoId = grupos[0].id, titulo = "Examen Parcial", descripcion = "Parcial backend Ktor", fechaEntrega = "2026-12-15"))
        }
        assertEquals(HttpStatusCode.Created, createActRes.status)
        val actividadCreada = createActRes.body<ApiResponse<Activity>>().data
        assertNotNull(actividadCreada)

        // Register Grade
        val gradeRes = client.post("/teacher/grades") {
            header("Authorization", "Bearer $docenteToken")
            contentType(ContentType.Application.Json)
            setBody(RegisterGradeRequest(actividadId = actividadCreada.id, estudianteId = 3, calificacion = 100.0, observaciones = "Nota perfecta"))
        }
        assertEquals(HttpStatusCode.OK, gradeRes.status)

        // Student Progress tracking by Teacher
        val progressRes = client.get("/teacher/students/3/progress") {
            header("Authorization", "Bearer $docenteToken")
        }
        assertEquals(HttpStatusCode.OK, progressRes.status)
        val progreso = progressRes.body<ApiResponse<AcademicProgress>>().data
        assertNotNull(progreso)
        assertEquals(3, progreso.estudianteId)
    }

    @Test
    fun testEstudianteConsultasYProgreso() = testApplication {
        val client = configurar()
        val estudianteToken = client.login("estudiante@focusup.com", "estudiante123")

        // Groups
        val groupsRes = client.get("/student/groups") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, groupsRes.status)

        // Teachers
        val teachersRes = client.get("/student/teachers") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, teachersRes.status)

        // Activities
        val activitiesRes = client.get("/student/activities") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, activitiesRes.status)

        // Grades
        val gradesRes = client.get("/student/grades") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, gradesRes.status)

        // Progress
        val progressRes = client.get("/student/progress") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.OK, progressRes.status)
        val progreso = progressRes.body<ApiResponse<AcademicProgress>>().data
        assertNotNull(progreso)
    }

    @Test
    fun testValidacionPermisosRBAC() = testApplication {
        val client = configurar()
        val estudianteToken = client.login("estudiante@focusup.com", "estudiante123")

        // Access /admin/users without token -> 401
        val noAuthRes = client.get("/admin/users")
        assertEquals(HttpStatusCode.Unauthorized, noAuthRes.status)

        // Student attempts to access /admin/users -> 403 Forbidden
        val forbiddenAdminRes = client.get("/admin/users") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.Forbidden, forbiddenAdminRes.status)

        // Student attempts to access /teacher/groups -> 403 Forbidden
        val forbiddenTeacherRes = client.get("/teacher/groups") {
            header("Authorization", "Bearer $estudianteToken")
        }
        assertEquals(HttpStatusCode.Forbidden, forbiddenTeacherRes.status)
    }
}
