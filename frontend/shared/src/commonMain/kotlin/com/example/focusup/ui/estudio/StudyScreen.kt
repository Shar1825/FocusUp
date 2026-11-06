package com.example.focusup.ui.estudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.EvidenciaResumen
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.Rol
import com.example.focusup.ui.theme.BordeTarjeta
import com.example.focusup.ui.theme.BordeTarjetaNeon
import com.example.focusup.ui.theme.FocusUpColors

private val JpegMuestraBytes = byteArrayOf(
    0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00.toByte(), 0x10.toByte(),
    0x4A.toByte(), 0x46.toByte(), 0x49.toByte(), 0x46.toByte(), 0x00.toByte(), 0x01.toByte(),
    0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x60.toByte(), 0x00.toByte(), 0x60.toByte(),
    0x00.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xDB.toByte(), 0x00.toByte(), 0x43.toByte(),
    0x00.toByte(), 0x08.toByte(), 0x06.toByte(), 0x06.toByte(), 0x07.toByte(), 0x06.toByte(),
    0x05.toByte(), 0x08.toByte(), 0x07.toByte(), 0x07.toByte(), 0x07.toByte(), 0x09.toByte(),
    0x09.toByte(), 0x08.toByte(), 0x0A.toByte(), 0x0C.toByte(), 0x14.toByte(), 0x0D.toByte(),
    0x0C.toByte(), 0x0B.toByte(), 0x0B.toByte(), 0x0C.toByte(), 0x19.toByte(), 0x12.toByte(),
    0x13.toByte(), 0x0F.toByte(), 0x14.toByte(), 0x1D.toByte(), 0x1A.toByte(), 0x1F.toByte(),
    0x1E.toByte(), 0x1D.toByte(), 0x1A.toByte(), 0x1C.toByte(), 0x1C.toByte(), 0x20.toByte(),
    0x24.toByte(), 0x2E.toByte(), 0x27.toByte(), 0x20.toByte(), 0x22.toByte(), 0x2C.toByte(),
    0x23.toByte(), 0x1C.toByte(), 0x1C.toByte(), 0x28.toByte(), 0x37.toByte(), 0x29.toByte(),
    0x2C.toByte(), 0x30.toByte(), 0x31.toByte(), 0x34.toByte(), 0x34.toByte(), 0x34.toByte(),
    0x1F.toByte(), 0x27.toByte(), 0x39.toByte(), 0x3D.toByte(), 0x38.toByte(), 0x32.toByte(),
    0x3C.toByte(), 0x2E.toByte(), 0x33.toByte(), 0x34.toByte(), 0x32.toByte(), 0xFF.toByte(),
    0xC0.toByte(), 0x00.toByte(), 0x0B.toByte(), 0x08.toByte(), 0x00.toByte(), 0x01.toByte(),
    0x00.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x11.toByte(), 0x00.toByte(),
    0xFF.toByte(), 0xC4.toByte(), 0x00.toByte(), 0x1F.toByte(), 0x00.toByte(), 0x00.toByte(),
    0x01.toByte(), 0x05.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(),
    0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x02.toByte(),
    0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(), 0x08.toByte(),
    0x09.toByte(), 0x0A.toByte(), 0x0B.toByte(), 0xFF.toByte(), 0xDA.toByte(), 0x00.toByte(),
    0x08.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x3F.toByte(),
    0x00.toByte(), 0x7F.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xD9.toByte()
)

@Composable
fun StudyScreen(
    usuario: LoginResponse,
    viewModel: StudyViewModel = remember { StudyViewModel() },
    onAbrirCamara: ((sesionId: Int) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarModalSubirEvidencia by remember { mutableStateOf(false) }
    var modalRechazoVisible by remember { mutableStateOf(false) }
    var idEvidenciaAValidar by remember { mutableStateOf<Int?>(null) }
    var comentarioDocenteInput by remember { mutableStateOf("") }
    var fotoAmpliada by remember { mutableStateOf<ImageBitmap?>(null) }

    val abrirCamaraNativa = rememberCameraLauncher { bytes ->
        viewModel.subirEvidencia(bytes)
    }

    val dispararCamara: () -> Unit = {
        if (onAbrirCamara != null && state.sesionIdActual != null) {
            onAbrirCamara(state.sesionIdActual!!)
        } else {
            abrirCamaraNativa()
        }
    }

    LaunchedEffect(usuario.id, usuario.rol) {
        if (usuario.rol == Rol.DOCENTE || usuario.rol == Rol.ADMINISTRADOR) {
            viewModel.cargarEvidenciasPendientesDocente()
        } else {
            viewModel.restaurarSesionEstudiante(usuario.id)
        }
    }

    LaunchedEffect(state.estadoSesion) {
        if (state.estadoSesion == EstadoSesionEstudio.ESPERANDO_FOTO) {
            dispararCamara()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Centro de Enfoque Pomodoro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Gestiona tu tiempo de estudio, valida tus sesiones y mantén tu concentración.",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusUpColors.TextoSecundario
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
                border = when (state.estadoSesion) {
                    EstadoSesionEstudio.APROBADA -> BorderStroke(1.5.dp, FocusUpColors.VerdeExito)
                    EstadoSesionEstudio.RECHAZADA -> BorderStroke(1.5.dp, FocusUpColors.Error)
                    EstadoSesionEstudio.EN_CURSO -> BordeTarjetaNeon
                    EstadoSesionEstudio.PENDIENTE_VALIDACION -> BorderStroke(1.5.dp, FocusUpColors.AzulCyan)
                    else -> BordeTarjeta
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EstadoBadge(estado = state.estadoSesion)

                    Spacer(modifier = Modifier.height(18.dp))

                    val totalSegundos = (state.duracionMinutos * 60).coerceAtLeast(1)
                    val progreso = (state.segundosRestantes.toFloat() / totalSegundos.toFloat()).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = FocusUpColors.Tarjeta,
                                style = Stroke(width = 14.dp.toPx())
                            )
                            drawArc(
                                color = when (state.estadoSesion) {
                                    EstadoSesionEstudio.APROBADA -> FocusUpColors.VerdeExito
                                    EstadoSesionEstudio.RECHAZADA -> FocusUpColors.Error
                                    EstadoSesionEstudio.PENDIENTE_VALIDACION -> FocusUpColors.AzulCyan
                                    else -> FocusUpColors.AzulElectrico
                                },
                                startAngle = -90f,
                                sweepAngle = 360f * progreso,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val min = state.segundosRestantes / 60
                            val sec = state.segundosRestantes % 60
                            val formatoTiempo = "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"

                            Text(
                                text = formatoTiempo,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FocusUpColors.Texto
                            )

                            Text(
                                text = when (state.estadoSesion) {
                                    EstadoSesionEstudio.CONFIGURACION -> "POR INICIAR"
                                    EstadoSesionEstudio.EN_CURSO -> "EN CURSO"
                                    EstadoSesionEstudio.PAUSADO -> "PAUSADO"
                                    EstadoSesionEstudio.ESPERANDO_FOTO -> "ENVIAR EVIDENCIA"
                                    EstadoSesionEstudio.SUBIENDO_EVIDENCIA -> "SUBIENDO..."
                                    EstadoSesionEstudio.PENDIENTE_VALIDACION -> "EN VALIDACIÓN"
                                    EstadoSesionEstudio.APROBADA -> "LIBERADO"
                                    EstadoSesionEstudio.RECHAZADA -> "RECHAZADA"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when (state.estadoSesion) {
                                    EstadoSesionEstudio.APROBADA -> FocusUpColors.VerdeExito
                                    EstadoSesionEstudio.RECHAZADA -> FocusUpColors.Error
                                    else -> FocusUpColors.AzulElectrico
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (state.estadoSesion) {
                        EstadoSesionEstudio.CONFIGURACION -> {
                            OutlinedTextField(
                                value = state.objetivo,
                                onValueChange = { viewModel.setObjetivo(it) },
                                label = { Text("Objetivo de estudio") },
                                placeholder = { Text("Ej. Resolución de ejercicios de cálculo") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusUpColors.AzulElectrico,
                                    unfocusedBorderColor = FocusUpColors.BordeCampo
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("Programación Kotlin", "Matemáticas", "Ing. de Software", "Bases de Datos")) { m ->
                                    val sel = state.objetivo == m
                                    SuggestionChip(
                                        onClick = { viewModel.setObjetivo(m) },
                                        label = { Text(m, fontSize = 12.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (sel) FocusUpColors.PurpuraNeon else FocusUpColors.Tarjeta,
                                            labelColor = if (sel) Color.White else FocusUpColors.TextoSecundario
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Selecciona la duración:",
                                style = MaterialTheme.typography.labelMedium,
                                color = FocusUpColors.TextoSecundario
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf(15, 25, 45, 60).forEach { min ->
                                    FilterChip(
                                        selected = state.duracionMinutos == min,
                                        onClick = { viewModel.setDuracion(min) },
                                        label = { Text("${min}m", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FocusUpColors.AzulElectrico,
                                            selectedLabelColor = FocusUpColors.Fondo,
                                            containerColor = FocusUpColors.Tarjeta
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.iniciarSesionPomodoro(usuario.id) },
                                enabled = !state.cargando,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
                            ) {
                                if (state.cargando) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = FocusUpColors.Fondo)
                                } else {
                                    Text("🚀 Iniciar Sesión de Estudio", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        EstadoSesionEstudio.EN_CURSO, EstadoSesionEstudio.PAUSADO -> {
                            Text(
                                text = state.objetivo.ifBlank { "Estudio Concentrado" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FocusUpColors.Texto
                            )
                            state.sesionIdActual?.let {
                                Text(
                                    text = "Sesión Activa #$it en Servidor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FocusUpColors.AzulCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (state.estadoSesion == EstadoSesionEstudio.EN_CURSO) {
                                    Button(
                                        onClick = { viewModel.pausarTimer() },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.Ambar)
                                    ) {
                                        Text("⏸ Pausar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.reanudarTimer() },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.VerdeExito)
                                    ) {
                                        Text("▶ Reanudar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.cancelarSesion() },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, FocusUpColors.Error)
                                ) {
                                    Text("⏹ Cancelar", color = FocusUpColors.Error, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = { viewModel.simularFinDeTiempoParaPruebas() }) {
                                Text("⚡ Simular término de tiempo (Pruebas 3s)", color = FocusUpColors.TextoSecundario, fontSize = 12.sp)
                            }
                        }

                        EstadoSesionEstudio.ESPERANDO_FOTO -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FocusUpColors.Tarjeta,
                                border = BorderStroke(1.dp, FocusUpColors.Ambar.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷 Tiempo de concentración completado", fontWeight = FontWeight.Bold, color = FocusUpColors.Ambar)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Para validar tus horas de estudio, toma una fotografía de tus notas o área de trabajo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusUpColors.TextoSecundario,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { dispararCamara() },
                                            colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("📸 Abrir Cámara", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { mostrarModalSubirEvidencia = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("📁 Muestra", color = FocusUpColors.TextoSecundario)
                                        }
                                    }
                                }
                            }
                        }

                        EstadoSesionEstudio.SUBIENDO_EVIDENCIA -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                CircularProgressIndicator(color = FocusUpColors.AzulElectrico)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Enviando fotografía al servidor Ktor...", color = FocusUpColors.Texto, fontWeight = FontWeight.Medium)
                            }
                        }

                        EstadoSesionEstudio.PENDIENTE_VALIDACION -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FocusUpColors.Tarjeta,
                                border = BorderStroke(1.dp, FocusUpColors.AzulCyan.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = FocusUpColors.AzulCyan, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Esperando validación del docente...", fontWeight = FontWeight.Bold, color = FocusUpColors.AzulCyan)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tu evidencia fue enviada correctamente. Esperando validación del docente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusUpColors.Texto,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Sesión #${state.sesionIdActual ?: 0} • Evidencia #${state.evidenciaIdActual ?: 0}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusUpColors.TextoSecundario
                                    )

                                    state.ultimaImagenBytes?.let { bytes ->
                                        byteArrayToImageBitmap(bytes)?.let { img ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Fotografía enviada:", style = MaterialTheme.typography.labelSmall, color = FocusUpColors.TextoSecundario)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Image(
                                                bitmap = img,
                                                contentDescription = "Evidencia Fotográfica",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { fotoAmpliada = img },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        EstadoSesionEstudio.APROBADA -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FocusUpColors.Tarjeta,
                                border = BorderStroke(1.5.dp, FocusUpColors.VerdeExito),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎉 ¡SESIÓN LIBERADA!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FocusUpColors.VerdeExito)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "¡Evidencia aprobada! Tu sesión de estudio ha finalizado y el dispositivo quedó liberado.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FocusUpColors.Texto,
                                        textAlign = TextAlign.Center
                                    )
                                    if (!state.comentarioDocente.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = FocusUpColors.TarjetaElevada,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Comentario del docente: \"${state.comentarioDocente}\"",
                                                modifier = Modifier.padding(10.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FocusUpColors.TextoSecundario
                                            )
                                        }
                                    }

                                    state.ultimaImagenBytes?.let { bytes ->
                                        byteArrayToImageBitmap(bytes)?.let { img ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Image(
                                                bitmap = img,
                                                contentDescription = "Evidencia Fotográfica Aprobada",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { fotoAmpliada = img },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.iniciarNuevaSesion() },
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.VerdeExito)
                                    ) {
                                        Text("✨ Iniciar Nueva Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        EstadoSesionEstudio.RECHAZADA -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FocusUpColors.ErrorFondo,
                                border = BorderStroke(1.5.dp, FocusUpColors.Error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚠️ EVIDENCIA RECHAZADA", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FocusUpColors.ErrorTexto)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "El docente ha rechazado la evidencia enviada y solicita una nueva fotografía para liberar tu sesión.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusUpColors.Texto,
                                        textAlign = TextAlign.Center
                                    )
                                    if (!state.comentarioDocente.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = FocusUpColors.Tarjeta,
                                            border = BorderStroke(1.dp, FocusUpColors.Error.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Motivo del docente: \"${state.comentarioDocente}\"",
                                                modifier = Modifier.padding(10.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FocusUpColors.ErrorTexto
                                            )
                                        }
                                    }

                                    state.ultimaImagenBytes?.let { bytes ->
                                        byteArrayToImageBitmap(bytes)?.let { img ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Image(
                                                bitmap = img,
                                                contentDescription = "Evidencia Fotográfica Rechazada",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { fotoAmpliada = img },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.reintentarCapturaFoto() },
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
                                    ) {
                                        Text("📸 Capturar Nueva Foto", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PANEL DOCENTE / ADMIN PARA REVISAR EVIDENCIAS
            if (usuario.rol == Rol.DOCENTE || usuario.rol == Rol.ADMINISTRADOR) {
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Panel Docente: Validación de Evidencias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FocusUpColors.Texto
                        )
                        Text(
                            text = "Revisa y aprueba o rechaza las sesiones enviadas por tus estudiantes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusUpColors.TextoSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.cargando) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FocusUpColors.AzulElectrico)
                    }
                } else if (state.evidenciasPendientes.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
                        border = BordeTarjeta
                    ) {
                        Text(
                            text = "✨ No hay evidencias pendientes de validación por el momento.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FocusUpColors.TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.evidenciasPendientes.forEach { ev ->
                            ItemEvidenciaDocente(
                                evidencia = ev,
                                onAprobar = { viewModel.validarEvidencia(ev.id, "APROBADA") },
                                onRechazar = {
                                    idEvidenciaAValidar = ev.id
                                    modalRechazoVisible = true
                                },
                                onVerFoto = { img -> fotoAmpliada = img }
                            )
                        }
                    }
                }
            }
        }

        // DIÁLOGO MODAL: FOTO AMPLIADA
        fotoAmpliada?.let { img ->
            AlertDialog(
                onDismissRequest = { fotoAmpliada = null },
                containerColor = FocusUpColors.TarjetaElevada,
                title = { Text("🔍 Evidencia Ampliada", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = img,
                            contentDescription = "Evidencia Completa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { fotoAmpliada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
                    ) {
                        Text("Cerrar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // DIÁLOGO MODAL: RECHAZO CON COMENTARIO
        if (modalRechazoVisible) {
            AlertDialog(
                onDismissRequest = { modalRechazoVisible = false },
                containerColor = FocusUpColors.TarjetaElevada,
                title = { Text("Rechazar Evidencia", color = FocusUpColors.ErrorTexto, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Indica la razón por la cual rechazas la evidencia fotográfica:",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusUpColors.TextoSecundario
                        )
                        OutlinedTextField(
                            value = comentarioDocenteInput,
                            onValueChange = { comentarioDocenteInput = it },
                            placeholder = { Text("Ej. La imagen no corresponde a las notas o no es legible.") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            idEvidenciaAValidar?.let { id ->
                                viewModel.validarEvidencia(id, "RECHAZADA", comentarioDocenteInput)
                            }
                            modalRechazoVisible = false
                            comentarioDocenteInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.Error)
                    ) {
                        Text("Confirmar Rechazo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { modalRechazoVisible = false }) {
                        Text("Cancelar", color = FocusUpColors.TextoSecundario)
                    }
                }
            )
        }

        // DIÁLOGO MODAL: SUBIR EVIDENCIA SIMULADA/MUESTRA
        if (mostrarModalSubirEvidencia) {
            AlertDialog(
                onDismissRequest = { mostrarModalSubirEvidencia = false },
                containerColor = FocusUpColors.TarjetaElevada,
                title = { Text("Enviar Evidencia Muestra", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Se enviará una imagen de prueba prediseñada al servidor para simular la captura de cámara en entornos de prueba.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusUpColors.TextoSecundario
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarModalSubirEvidencia = false
                            viewModel.subirEvidencia(JpegMuestraBytes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
                    ) {
                        Text("Enviar Evidencia Muestra", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarModalSubirEvidencia = false }) {
                        Text("Cancelar", color = FocusUpColors.TextoSecundario)
                    }
                }
            )
        }
    }
}

@Composable
private fun ItemEvidenciaDocente(
    evidencia: EvidenciaResumen,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onVerFoto: (ImageBitmap) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
        border = BorderStroke(1.dp, FocusUpColors.AzulCyan.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = evidencia.estudiante,
                    fontWeight = FontWeight.Bold,
                    color = FocusUpColors.Texto,
                    fontSize = 15.sp
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FocusUpColors.AzulCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Evidencia #${evidencia.id}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusUpColors.AzulCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Objetivo: ${evidencia.objetivo}",
                style = MaterialTheme.typography.bodySmall,
                color = FocusUpColors.TextoSecundario
            )

            Spacer(modifier = Modifier.height(10.dp))

            val imgBitmap = remember(evidencia.imagenBytes) {
                evidencia.imagenBytes?.let { byteArrayToImageBitmap(it) }
            }

            if (imgBitmap != null) {
                Image(
                    bitmap = imgBitmap,
                    contentDescription = "Foto de evidencia del estudiante",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onVerFoto(imgBitmap) },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusUpColors.TarjetaElevada)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷 Fotografía no precargada (Toca para ver)", style = MaterialTheme.typography.bodySmall, color = FocusUpColors.TextoSecundario)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAprobar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.VerdeExito),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✓ Aprobar", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, FocusUpColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✗ Rechazar...", color = FocusUpColors.Error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(estado: EstadoSesionEstudio) {
    val (colorBadge, textoBadge) = when (estado) {
        EstadoSesionEstudio.CONFIGURACION -> FocusUpColors.TextoSecundario to "🎯 LISTO PARA COMENZAR"
        EstadoSesionEstudio.EN_CURSO -> FocusUpColors.AzulElectrico to "⚡ EN CONCENTRACIÓN"
        EstadoSesionEstudio.PAUSADO -> FocusUpColors.Ambar to "⏸ TEMPORIZADOR PAUSADO"
        EstadoSesionEstudio.ESPERANDO_FOTO -> FocusUpColors.Ambar to "📸 ESPERANDO EVIDENCIA"
        EstadoSesionEstudio.SUBIENDO_EVIDENCIA -> FocusUpColors.AzulElectrico to "📤 ENVIANDO FOTO..."
        EstadoSesionEstudio.PENDIENTE_VALIDACION -> FocusUpColors.AzulCyan to "⏳ EN REVISIÓN POR DOCENTE"
        EstadoSesionEstudio.APROBADA -> FocusUpColors.VerdeExito to "🎉 SESIÓN APROBADA Y LIBERADA"
        EstadoSesionEstudio.RECHAZADA -> FocusUpColors.Error to "⚠️ EVIDENCIA RECHAZADA"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorBadge.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, colorBadge.copy(alpha = 0.4f))
    ) {
        Text(
            text = textoBadge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colorBadge,
            fontWeight = FontWeight.Bold
        )
    }
}