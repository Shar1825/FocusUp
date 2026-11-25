package com.focusup.services

import com.focusup.dto.*
import com.focusup.model.Rol
import com.focusup.model.UserDto
import com.focusup.repository.ActividadRepository
import com.focusup.repository.EstudioRepository
import com.focusup.repository.GrupoRepository
import com.focusup.repository.UsuarioRepository

object AdminService {

    fun getUsers(query: String? = null, rol: Rol? = null): List<UserDto> {
        return UsuarioRepository.obtenerTodos(query, rol).map { u ->
            UserDto(u.id, u.nombre, u.correo, u.rol, u.activo)
        }
    }

    fun createUser(req: CreateUserRequest): UserDto {
        val u = UsuarioRepository.crearUsuario(req.nombre, req.correo, req.password, req.rol)
        return UserDto(u.id, u.nombre, u.correo, u.rol, u.activo)
    }

    fun getUserById(id: Int): UserDto? {
        val u = UsuarioRepository.buscarPorId(id) ?: return null
        return UserDto(u.id, u.nombre, u.correo, u.rol, u.activo)
    }

    fun updateUser(id: Int, req: UpdateUserRequest): UserDto? {
        val u = UsuarioRepository.actualizarUsuarioPorAdmin(id, req.nombre, req.correo, req.rol, req.password) ?: return null
        return UserDto(u.id, u.nombre, u.correo, u.rol, u.activo)
    }

    fun updateUserStatus(id: Int, req: UpdateUserStatusRequest): UserDto? {
        val u = UsuarioRepository.cambiarEstado(id, req.activo) ?: return null
        return UserDto(u.id, u.nombre, u.correo, u.rol, u.activo)
    }

    fun getStatistics(): AdminStatisticsResponse {
        val usuarios = UsuarioRepository.obtenerTodos()
        val totalUsuarios = usuarios.size
        val admins = usuarios.count { it.rol.esAdmin() }
        val docentes = usuarios.count { it.rol == Rol.DOCENTE }
        val estudiantes = usuarios.count { it.rol == Rol.ESTUDIANTE }
        val activos = usuarios.count { it.activo }
        val inactivos = totalUsuarios - activos

        val activosAdmin = usuarios.count { it.rol.esAdmin() && it.activo }
        val activosDocentes = usuarios.count { it.rol == Rol.DOCENTE && it.activo }
        val activosEstudiantes = usuarios.count { it.rol == Rol.ESTUDIANTE && it.activo }

        val totalGrupos = GrupoRepository.contarTotal()
        val totalActividades = ActividadRepository.contarTotal()

        val totalSesiones = EstudioRepository.contarTotalSesiones()
        val evPendientes = EstudioRepository.contarEvidenciasPorEstado("PENDIENTE")
        val evAprobadas = EstudioRepository.contarEvidenciasPorEstado("APROBADA")
        val evRechazadas = EstudioRepository.contarEvidenciasPorEstado("RECHAZADA")

        return AdminStatisticsResponse(
            totalUsuarios = totalUsuarios,
            cantidadAdmin = admins,
            cantidadDocentes = docentes,
            cantidadEstudiantes = estudiantes,
            usuariosActivos = activos,
            usuariosInactivos = inactivos,
            usuariosActivosAdmin = activosAdmin,
            usuariosActivosDocente = activosDocentes,
            usuariosActivosEstudiante = activosEstudiantes,
            totalGrupos = totalGrupos,
            totalActividades = totalActividades,
            totalSesiones = totalSesiones,
            evidenciasPendientes = evPendientes,
            evidenciasAprobadas = evAprobadas,
            evidenciasRechazadas = evRechazadas
        )
    }
}
