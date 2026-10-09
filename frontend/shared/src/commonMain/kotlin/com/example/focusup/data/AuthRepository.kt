package com.example.focusup.data

import com.example.focusup.model.LoginResponse
import com.example.focusup.network.AuthService

/**
 * Contrato de acceso a datos de autenticación.
 * El ViewModel depende de esta interfaz, no de Ktor: así se puede reemplazar
 * por una versión falsa en las pruebas unitarias.
 */
interface AuthRepository {
    suspend fun login(correo: String, password: String): Result<LoginResponse>
}

/** Implementación real: delega en [AuthService] (Ktor Client). */
class ApiAuthRepository(
    private val service: AuthService = AuthService()
) : AuthRepository {
    override suspend fun login(correo: String, password: String): Result<LoginResponse> =
        service.login(correo, password)
}