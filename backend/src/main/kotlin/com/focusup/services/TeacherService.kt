package com.focusup.services

import com.focusup.dto.CreateActivityRequest
import com.focusup.dto.RegisterGradeRequest
import com.focusup.dto.UpdateActivityRequest
import com.focusup.model.*
import com.focusup.repository.ActividadRepository
import com.focusup.repository.CalificacionRepository
import com.focusup.repository.GrupoRepository

object TeacherService {

    fun getGroups(docenteId: Int): List<Group> {
        return GrupoRepository.obtenerPorDocenteId(docenteId)
    }

    fun getStudents(docenteId: Int): List<UserDto> {
        return GrupoRepository.obtenerEstudiantesDeGruposDocente(docenteId).map {
            UserDto(it.id, it.nombre, it.correo, it.rol, it.activo)
        }
    }

    fun getActivities(docenteId: Int): List<Activity> {
        return ActividadRepository.obtenerPorDocenteId(docenteId)
    }

    fun createActivity(docenteId: Int, req: CreateActivityRequest): Activity {
        return ActividadRepository.crearActividad(docenteId, req.grupoId, req.titulo, req.descripcion, req.fechaEntrega)
    }

    fun updateActivity(docenteId: Int, id: Int, req: UpdateActivityRequest): Activity? {
        return ActividadRepository.actualizarActividad(id, docenteId, req.titulo, req.descripcion, req.fechaEntrega)
    }

    fun getGrades(docenteId: Int): List<Grade> {
        return CalificacionRepository.obtenerPorDocenteId(docenteId)
    }

    fun registerGrade(docenteId: Int, req: RegisterGradeRequest): Grade {
        return CalificacionRepository.registrarOActualizarCalificacion(
            docenteId, req.actividadId, req.estudianteId, req.calificacion, req.observaciones
        )
    }

    fun getStudentProgress(docenteId: Int, estudianteId: Int): AcademicProgress {
        val estudiantesAsignados = GrupoRepository.obtenerEstudiantesDeGruposDocente(docenteId).map { it.id }
        if (estudianteId !in estudiantesAsignados) {
            throw IllegalArgumentException("El estudiante no está asignado a ninguno de sus grupos.")
        }
        return CalificacionRepository.calcularProgresoEstudiante(estudianteId)
    }
}
