package com.example.focusup.data

import com.example.focusup.model.EvidenciaResumen
import com.example.focusup.ui.estudio.EstadoSesionEstudio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EstadoSesionGuardada(
    val estudianteId: Int,
    val sesionId: Int,
    val evidenciaId: Int?,
    val objetivo: String,
    val duracionMinutos: Int,
    val segundosRestantes: Int,
    val estadoSesion: EstadoSesionEstudio,
    val ultimaImagenBytes: ByteArray?,
    val comentarioDocente: String?
)

object StudyRepository {

    private val _evidenciasPendientes = MutableStateFlow<List<EvidenciaResumen>>(emptyList())
    val evidenciasPendientes: StateFlow<List<EvidenciaResumen>> = _evidenciasPendientes.asStateFlow()

    private val _sesionesEstudiantes = MutableStateFlow<Map<Int, EstadoSesionGuardada>>(emptyMap())
    val sesionesEstudiantes: StateFlow<Map<Int, EstadoSesionGuardada>> = _sesionesEstudiantes.asStateFlow()

    fun guardarSesionEstudiante(estado: EstadoSesionGuardada) {
        _sesionesEstudiantes.update { it + (estado.estudianteId to estado) }
    }

    fun obtenerSesionEstudiante(estudianteId: Int): EstadoSesionGuardada? {
        return _sesionesEstudiantes.value[estudianteId]
    }

    fun agregarEvidenciaPendiente(evidencia: EvidenciaResumen) {
        _evidenciasPendientes.update { lista ->
            listOf(evidencia) + lista.filter { it.id != evidencia.id }
        }
    }

    fun removerEvidenciaPendiente(evidenciaId: Int) {
        _evidenciasPendientes.update { lista ->
            lista.filter { it.id != evidenciaId }
        }
    }

    fun actualizarEstadoEvidencia(evidenciaId: Int, nuevoEstado: String, comentario: String?) {
        _sesionesEstudiantes.update { mapa ->
            mapa.mapValues { (_, sesion) ->
                if (sesion.evidenciaId == evidenciaId) {
                    val nuevoEstadoSesion = when (nuevoEstado.uppercase()) {
                        "APROBADA" -> EstadoSesionEstudio.APROBADA
                        "RECHAZADA" -> EstadoSesionEstudio.RECHAZADA
                        else -> sesion.estadoSesion
                    }
                    sesion.copy(
                        estadoSesion = nuevoEstadoSesion,
                        comentarioDocente = comentario
                    )
                } else {
                    sesion
                }
            }
        }
        _evidenciasPendientes.update { lista ->
            lista.map { ev ->
                if (ev.id == evidenciaId) ev.copy(estado = nuevoEstado) else ev
            }
        }
    }
}
