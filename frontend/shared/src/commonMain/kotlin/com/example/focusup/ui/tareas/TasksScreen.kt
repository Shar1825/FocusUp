package com.example.focusup.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.Activity
import com.example.focusup.model.CreateActivityRequest
import com.example.focusup.model.Group
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.RegisterGradeRequest
import com.example.focusup.model.Rol
import com.example.focusup.model.UserDto
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

                // El botón para crear actividades solo se muestra para el rol DOCENTE
                if (usuario.rol == Rol.DOCENTE) {
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

            // Indicador de carga / Estado de error sin conexión / Lista de actividades
            if (state.cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FocusUpColors.AzulElectrico)
                }
            } else if (state.error != null && state.actividades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
                        border = BorderStroke(1.dp, FocusUpColors.Error)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("⚠️ Sin conexión", fontWeight = FontWeight.Bold, color = FocusUpColors.ErrorTexto, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error ?: "Sin conexión con el servidor. Verifica tu conexión e intenta de nuevo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FocusUpColors.TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.cargarActividades(usuario.rol) },
                                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🔄 Reintentar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                            esDocente = usuario.rol == Rol.DOCENTE,
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
            if (state.actividades.isNotEmpty()) {
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
    }

    // Modal Crear Actividad (Docente)
    if (mostrarModalCrear && usuario.rol == Rol.DOCENTE) {
        LaunchedEffect(Unit) {
            viewModel.cargarGruposDocente()
        }
        ModalCrearActividad(
            grupos = state.grupos,
            cargandoGrupos = state.cargandoModal,
            onDesestimar = { mostrarModalCrear = false },
            onGuardar = { req ->
                viewModel.crearActividad(req, usuario.rol)
                mostrarModalCrear = false
            }
        )
    }

    // Modal Calificar Estudiante (Docente)
    actividadParaCalificar?.let { act ->
        if (usuario.rol == Rol.DOCENTE) {
            LaunchedEffect(act.id) {
                viewModel.cargarEstudiantesDocente()
            }
            ModalCalificarActividad(
                actividad = act,
                estudiantes = state.estudiantes,
                cargandoEstudiantes = state.cargandoModal,
                onDesestimar = { actividadParaCalificar = null },
                onRegistrar = { estId, nota, obs ->
                    viewModel.registrarCalificacion(
                        RegisterGradeRequest(
                            actividadId = act.id,
                            estudianteId = estId,
                            calificacion = nota,
                            observaciones = obs
                        )
                    )
                    actividadParaCalificar = null
                }
            )
        }
    }
}

/** Componente Selector Personalizado multi-plataforma */
@Composable
private fun <T> CustomSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedItem?.let { itemLabel(it) } ?: "Seleccionar $label...",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = FocusUpColors.AzulElectrico)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusUpColors.AzulElectrico,
                unfocusedBorderColor = FocusUpColors.BordeCampo,
                disabledBorderColor = FocusUpColors.BordeCampo,
                disabledTextColor = FocusUpColors.Texto,
                disabledLabelColor = FocusUpColors.TextoSecundario
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(FocusUpColors.TarjetaElevada)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item), color = FocusUpColors.Texto) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
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
    grupos: List<Group>,
    cargandoGrupos: Boolean,
    onDesestimar: () -> Unit,
    onGuardar: (CreateActivityRequest) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("2025-11-15") }
    var grupoSeleccionado by remember(grupos) { mutableStateOf(grupos.firstOrNull()) }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("➕ Nueva Actividad Académica", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (cargandoGrupos) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FocusUpColors.AzulElectrico)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cargando grupos del docente...", color = FocusUpColors.TextoSecundario)
                    }
                } else if (grupos.isEmpty()) {
                    Text(
                        "⚠️ No se encontraron grupos asignados a tu cuenta. Se requiere al menos un grupo activo para publicar una actividad.",
                        color = FocusUpColors.Ambar,
                        fontSize = 13.sp
                    )
                } else {
                    CustomSelector(
                        label = "Grupo / Materia",
                        items = grupos,
                        selectedItem = grupoSeleccionado,
                        itemLabel = { "${it.nombre} (${it.codigo.ifBlank { "PROG" }})" },
                        onItemSelected = { grupoSeleccionado = it }
                    )
                }

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        if (it.isNotBlank()) errorValidacion = null
                    },
                    label = { Text("Título de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                OutlinedTextField(
                    value = fechaEntrega,
                    onValueChange = {
                        fechaEntrega = it
                        if (it.isNotBlank()) errorValidacion = null
                    },
                    label = { Text("Fecha de entrega (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                errorValidacion?.let { err ->
                    Text(err, color = FocusUpColors.Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val grupoId = grupoSeleccionado?.id ?: 0
                    when {
                        grupoId <= 0 -> errorValidacion = "Por favor selecciona un grupo para la actividad."
                        titulo.isBlank() -> errorValidacion = "El título de la actividad es obligatorio."
                        fechaEntrega.isBlank() -> errorValidacion = "La fecha de entrega es obligatoria."
                        else -> {
                            onGuardar(
                                CreateActivityRequest(
                                    grupoId = grupoId,
                                    titulo = titulo.trim(),
                                    descripcion = descripcion.trim(),
                                    fechaEntrega = fechaEntrega.trim()
                                )
                            )
                        }
                    }
                },
                enabled = !cargandoGrupos && grupos.isNotEmpty(),
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
    estudiantes: List<UserDto>,
    cargandoEstudiantes: Boolean,
    onDesestimar: () -> Unit,
    onRegistrar: (estudianteId: Int, calificacion: Double, observaciones: String) -> Unit
) {
    var estudianteSeleccionado by remember(estudiantes) { mutableStateOf(estudiantes.firstOrNull()) }
    var notaTexto by remember { mutableStateOf("9.5") }
    var observaciones by remember { mutableStateOf("Buen desempeño.") }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("📝 Calificar: ${actividad.titulo}", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Grupo: ${actividad.grupoNombre.ifBlank { "Programación Móvil" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusUpColors.AzulCyan,
                    fontWeight = FontWeight.Bold
                )

                if (cargandoEstudiantes) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FocusUpColors.AzulElectrico)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cargando lista de estudiantes...", color = FocusUpColors.TextoSecundario)
                    }
                } else if (estudiantes.isEmpty()) {
                    Text(
                        "⚠️ No hay estudiantes registrados en este grupo para calificar.",
                        color = FocusUpColors.Ambar,
                        fontSize = 13.sp
                    )
                } else {
                    CustomSelector(
                        label = "Estudiante",
                        items = estudiantes,
                        selectedItem = estudianteSeleccionado,
                        itemLabel = { "${it.nombre} (${it.correo})" },
                        onItemSelected = { estudianteSeleccionado = it }
                    )
                }

                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = {
                        notaTexto = it
                        errorValidacion = null
                    },
                    label = { Text("Calificación (0.0 - 10.0)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusUpColors.AzulElectrico,
                        unfocusedBorderColor = FocusUpColors.BordeCampo
                    )
                )

                errorValidacion?.let { err ->
                    Text(err, color = FocusUpColors.Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val estudianteId = estudianteSeleccionado?.id ?: 0
                    val nota = notaTexto.toDoubleOrNull()

                    when {
                        estudianteId <= 0 -> errorValidacion = "Por favor selecciona un estudiante a calificar."
                        nota == null -> errorValidacion = "La calificación debe ser un valor numérico (ej. 9.5)."
                        nota < 0.0 || nota > 10.0 -> errorValidacion = "La calificación debe estar entre 0.0 y 10.0."
                        else -> {
                            onRegistrar(estudianteId, nota, observaciones.trim())
                        }
                    }
                },
                enabled = !cargandoEstudiantes && estudiantes.isNotEmpty(),
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
