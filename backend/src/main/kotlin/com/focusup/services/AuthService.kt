package com.focusup.services

import com.focusup.authentication.JwtManager
import com.focusup.dto.AuthSuccessResponse
import com.focusup.dto.LoginResponseData
import com.focusup.model.LoginRequest
import com.focusup.model.LoginResponse
import com.focusup.repository.UsuarioRepository

object AuthService {

    fun login(request: LoginRequest): Pair<AuthSuccessResponse, LoginResponse>? {
        val correo = request.correoEfectivo
        val usuario = UsuarioRepository.buscarPorCorreoYPassword(correo, request.password) ?: return null

        val token = JwtManager.generateToken(usuario)

        val authSuccess = AuthSuccessResponse(
            success = true,
            message = "Inicio de sesión correcto",
            user = LoginResponseData(
                id = usuario.id,
                name = usuario.nombre,
                email = usuario.correo,
                role = usuario.rol,
                active = usuario.activo
            ),
            token = token
        )

        val legacyResponse = LoginResponse(
            id = usuario.id,
            nombre = usuario.nombre,
            correo = usuario.correo,
            rol = usuario.rol
        )

        return Pair(authSuccess, legacyResponse)
    }
}
