package com.focusup.services

import com.focusup.dto.ChangePasswordRequest
import com.focusup.dto.UpdateProfileRequest
import com.focusup.model.*
import com.focusup.repository.CalificacionRepository
import com.focusup.repository.GrupoRepository
import com.focusup.repository.UsuarioRepository

object ProfileService {

    fun getProfile(userId: Int): Any? {
        val user = UsuarioRepository.buscarPorId(userId) ?: return null
        return when (user.rol) {
            Rol.ADMINISTRADOR, Rol.ADMIN -> AdminProfile(
                id = user.id,
                nombre = user.nombre,
                correo = user.correo,
                rol = Rol.ADMINISTRADOR,
            )
            Rol.DOCENTE -> {
                val grupos = GrupoRepository.obtenerPorDocenteId(userId)
                val estudiantes = GrupoRepository.obtenerEstudiantesDeGruposDocente(userId)
                TeacherProfile(
                    id = user.id,
                    nombre = user.nombre,
                    correo = user.correo,
                    rol = Rol.DOCENTE,
                    gruposAsignadosCount = grupos.size,
                    totalEstudiantesCount = estudiantes.size
                )
            }
            Rol.ESTUDIANTE -> {
                val progreso = CalificacionRepository.calcularProgresoEstudiante(userId)
                StudentProfile(
                    id = user.id,
                    nombre = user.nombre,
                    correo = user.correo,
                    rol = Rol.ESTUDIANTE,
                    promedioGeneral = progreso.promedioCalificaciones,
                    actividadesCompletadas = progreso.actividadesCompletadas
                )
            }
        }
    }

    fun updateProfile(userId: Int, req: UpdateProfileRequest): UserDto? {
        val actualizado = UsuarioRepository.actualizarPerfil(userId, req.nombre, req.correo) ?: return null
        return UserDto(
            id = actualizado.id,
            nombre = actualizado.nombre,
            correo = actualizado.correo,
            rol = actualizado.rol,
            activo = actualizado.activo
        )
    }

    fun changePassword(userId: Int, req: ChangePasswordRequest): Boolean {
        return UsuarioRepository.cambiarPassword(userId, req.passwordActual, req.nuevaPassword)
    }
}
