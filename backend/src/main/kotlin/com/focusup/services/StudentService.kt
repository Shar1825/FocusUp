package com.focusup.services

import com.focusup.dto.TeacherInfo
import com.focusup.model.AcademicProgress
import com.focusup.model.Activity
import com.focusup.model.Grade
import com.focusup.model.Group
import com.focusup.repository.ActividadRepository
import com.focusup.repository.CalificacionRepository
import com.focusup.repository.GrupoRepository
import com.focusup.repository.UsuarioRepository

object StudentService {

    fun getGroups(estudianteId: Int): List<Group> {
        return GrupoRepository.obtenerPorEstudianteId(estudianteId)
    }

    fun getTeachers(estudianteId: Int): List<TeacherInfo> {
        val grupos = GrupoRepository.obtenerPorEstudianteId(estudianteId)
        return grupos.mapNotNull { g ->
            val docente = UsuarioRepository.buscarPorId(g.docenteId)
            if (docente != null) {
                TeacherInfo(
                    id = docente.id,
                    nombre = docente.nombre,
                    correo = docente.correo,
                    grupoNombre = g.nombre
                )
            } else null
        }
    }

    fun getActivities(estudianteId: Int): List<Activity> {
        return ActividadRepository.obtenerPorEstudianteId(estudianteId)
    }

    fun getGrades(estudianteId: Int): List<Grade> {
        return CalificacionRepository.obtenerPorEstudianteId(estudianteId)
    }

    fun getProgress(estudianteId: Int): AcademicProgress {
        return CalificacionRepository.calcularProgresoEstudiante(estudianteId)
    }
}
