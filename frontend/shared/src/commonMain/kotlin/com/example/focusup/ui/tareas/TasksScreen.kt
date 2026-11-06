package com.example.focusup.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.Activity
import com.example.focusup.model.CreateActivityRequest
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.RegisterGradeRequest
import com.example.focusup.model.Rol
import com.example.focusup.ui.theme.BordeTarjeta
import com.example.focusup.ui.theme.BordeTarjetaNeon
import com.example.focusup.ui.theme.FocusUpColors

/**
 * Pantalla del Módulo de Tareas y Actividades Académicas.
 * Soporta vistas diferenciadas para Estudiantes y Docentes.
 */
@Composable
fun TasksScreen(
    usuario: LoginResponse,
    viewModel: TasksViewModel = remember { TasksViewModel() }
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarModalCrear by remember { mutableStateOf(false) }
    var actividadParaCalificar by remember { mutableStateOf<Activity?>(null) }

    LaunchedEffect(usuario.rol) {
        viewModel.cargarActividades(usuario.rol)
    }

    val actividadesFiltradas = remember(state.actividades, state.filtroEstado) {
        when (state.filtroEstado) {
            "Pendientes" -> state.actividades.take(4)
            "Recientes" -> state.actividades.takeLast(3)
            else -> state.actividades
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Cabecera del Módulo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (usuario.rol == Rol.DOCENTE) "Gestión de Actividades (Docente)" else "Mis Tareas Académicas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FocusUpColors.Texto,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = if (usuario.rol == Rol.DOCENTE) "Crea y supervisa entregas de tus grupos." else "Revisa tus entregas y mantente al día.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusUpColors.TextoSecundario
                    )
                }

                if (usuario.rol == Rol.DOCENTE || usuario.rol == Rol.ADMINISTRADOR) {
                    IconButton(
                        onClick = { mostrarModalCrear = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FocusUpColors.AzulElectrico)
                    ) {
                        Text("➕", color = FocusUpColors.Fondo, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de estado
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Todas", "Pendientes", "Recientes")) { filtro ->
                    val sel = state.filtroEstado == filtro
                    FilterChip(
                        selected = sel,
                        onClick = { viewModel.setFiltro(filtro) },
                        label = { Text(filtro, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FocusUpColors.AzulElectrico,
                            selectedLabelColor = FocusUpColors.Fondo,
                            containerColor = FocusUpColors.Tarjeta,
                            labelColor = FocusUpColors.Texto
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje informativo o cargando
            if (state.cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FocusUpColors.AzulElectrico)
                }
            } else if (actividadesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades registradas en esta sección.",
                        color = FocusUpColors.TextoSecundario,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(actividadesFiltradas, key = { it.id }) { act ->
                        ItemActividadAcademica(
                            actividad = act,
                            esDocente = usuario.rol == Rol.DOCENTE || usuario.rol == Rol.ADMINISTRADOR,
                            onCalificar = { actividadParaCalificar = act }
                        )
                    }
                }
            }
        }

        // Feedback / Notificaciones
        state.mensaje?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = FocusUpColors.TarjetaElevada,
                border = BordeTarjetaNeon
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓ $msg", color = FocusUpColors.VerdeExito, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.limpiarMensajes() }) {
                        Text("OK", color = FocusUpColors.AzulElectrico)
                    }
                }
            }
        }

        state.error?.let { err ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = FocusUpColors.ErrorFondo,
                border = BorderStroke(1.dp, FocusUpColors.Error)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️ $err", color = FocusUpColors.ErrorTexto, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.limpiarMensajes() }) {
                        Text("Cerrar", color = FocusUpColors.Texto)
                    }
                }
            }
        }
    }

    // Modal Crear Actividad (Docente)
    if (mostrarModalCrear) {
        ModalCrearActividad(
            onDesestimar = { mostrarModalCrear = false },
            onGuardar = { req ->
                viewModel.crearActividad(req)
                mostrarModalCrear = false
            }
        )
    }

    // Modal Calificar Estudiante (Docente)
    actividadParaCalificar?.let { act ->
        ModalCalificarActividad(
            actividad = act,
            onDesestimar = { actividadParaCalificar = null },
            onRegistrar = { nota, obs ->
                viewModel.registrarCalificacion(
                    RegisterGradeRequest(
                        actividadId = act.id,
                        estudianteId = 1,
                        calificacion = nota,
                        observaciones = obs
                    )
                )
                actividadParaCalificar = null
            }
        )
    }
}

/** Tarjeta futurista para representar una tarea o actividad */
@Composable
private fun ItemActividadAcademica(
    actividad: Activity,
    esDocente: Boolean,
    onCalificar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
        border = BordeTarjeta
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actividad.grupoNombre.ifBlank { "Materia General" },
                    style = MaterialTheme.typography.labelSmall,
                    color = FocusUpColors.AzulElectrico,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FocusUpColors.TarjetaElevada,
                    border = BorderStroke(1.dp, FocusUpColors.BordeCampo)
                ) {
                    Text(
                        text = "Entrega: ${actividad.fechaEntrega.ifBlank { "Pendiente" }}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusUpColors.TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = actividad.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto
            )

            if (actividad.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusUpColors.TextoSecundario,
                    maxLines = 2
                )
            }

            if (esDocente) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCalificar,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FocusUpColors.AzulElectrico)
                    ) {
                        Text("📝 Calificar", color = FocusUpColors.AzulElectrico, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/** Diálogo modal para que el docente cree una actividad */
@Composable
private fun ModalCrearActividad(
    onDesestimar: () -> Unit,
    onGuardar: (CreateActivityRequest) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("2024-11-15") }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("➕ Nueva Actividad Académica", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la tarea") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Instrucciones") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                OutlinedTextField(
                    value = fechaEntrega,
                    onValueChange = { fechaEntrega = it },
                    label = { Text("Fecha de entrega (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onGuardar(
                            CreateActivityRequest(
                                grupoId = 1,
                                titulo = titulo,
                                descripcion = descripcion,
                                fechaEntrega = fechaEntrega
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Publicar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDesestimar) {
                Text("Cancelar", color = FocusUpColors.TextoSecundario)
            }
        }
    )
}

/** Diálogo modal para registrar una calificación */
@Composable
private fun ModalCalificarActividad(
    actividad: Activity,
    onDesestimar: () -> Unit,
    onRegistrar: (calificacion: Double, observaciones: String) -> Unit
) {
    var notaTexto by remember { mutableStateOf("9.5") }
    var observaciones by remember { mutableStateOf("Buen desempeño.") }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("📝 Calificar: ${actividad.titulo}", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = { notaTexto = it },
                    label = { Text("Calificación (0 - 10)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones / Retroalimentación") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nota = notaTexto.toDoubleOrNull() ?: 10.0
                    onRegistrar(nota, observaciones)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Guardar Nota", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDesestimar) {
                Text("Cancelar", color = FocusUpColors.TextoSecundario)
            }
        }
    )
}
