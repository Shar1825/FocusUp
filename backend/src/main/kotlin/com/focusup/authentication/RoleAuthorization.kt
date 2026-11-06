package com.focusup.authentication

import com.focusup.model.Rol
import com.focusup.utils.ApiErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * Ejecuta el bloque solo si la llamada incluye un token válido.
 * Si falla, responde con HTTP 401 Unauthorized.
 */
suspend inline fun ApplicationCall.requireAuthentication(
    crossinline block: suspend (userPayload: JwtPayload) -> Unit,
) {
    val payload = getAuthenticatedUser()
    if (payload == null) {
        respond(
            HttpStatusCode.Unauthorized,
            ApiErrorResponse(message = "Acceso no autorizado. Debe iniciar sesión primero.")
        )
        return
    }
    block(payload)
}

/**
 * Ejecuta el bloque solo si el usuario autenticado posee alguno de los roles indicados.
 * Responde 401 si no está autenticado, o 403 Forbidden si su rol no tiene permiso.
 */
suspend inline fun ApplicationCall.requireRole(
    vararg allowedRoles: Rol,
    crossinline block: suspend (userPayload: JwtPayload) -> Unit,
) {
    requireAuthentication { payload ->
        val userRole = try {
            Rol.valueOf(payload.role)
        } catch (_: Exception) {
            null
        }

        val match = allowedRoles.any { allowed ->
            (allowed == userRole) ||
                    (allowed == Rol.ADMINISTRADOR && userRole == Rol.ADMIN) ||
                    (allowed == Rol.ADMIN && userRole == Rol.ADMINISTRADOR)
        }

        if (userRole == null || !match) {
            respond(
                HttpStatusCode.Forbidden,
                ApiErrorResponse(message = "Permiso denegado. Se requiere rol: ${allowedRoles.joinToString { it.name }}")
            )
            return@requireAuthentication
        }

        block(payload)
    }
}
