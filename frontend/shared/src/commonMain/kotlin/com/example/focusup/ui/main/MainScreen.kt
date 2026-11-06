package com.example.focusup.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.Rol
import com.example.focusup.ui.estudio.StudyScreen
import com.example.focusup.ui.estudio.StudyViewModel
import com.example.focusup.ui.tareas.TasksScreen
import com.example.focusup.ui.theme.BordeTarjeta
import com.example.focusup.ui.theme.BordeTarjetaNeon
import com.example.focusup.ui.theme.FocusUpColors

/** Tabs de navegación inferior */
enum class MainTab(val titulo: String, val icono: String) {
    INICIO("Inicio", "🏠"),
    ESTUDIO("Estudio", "⏱️"),
    TAREAS("Tareas", "📋"),
    PERFIL("Perfil", "👤")
}

/** Tarea local para la UI de demostración/interacción */
data class TareaUI(
    val id: Int,
    val titulo: String,
    val materia: String,
    val fechaEntrega: String,
    val prioridad: String,
    val completada: Boolean = false
)

/** Actividad del día para la UI */
data class ActividadDiaUI(
    val id: Int,
    val hora: String,
    val titulo: String,
    val tipo: String,
    val estado: String
)

/** Contenido adaptativo según rol */
private data class PanelRolInfo(
    val titulo: String,
    val descripcion: String,
    val acciones: List<String>,
    val metricasClave: List<Pair<String, String>>
)

private fun panelPara(rol: Rol): PanelRolInfo = when (rol) {
    Rol.ADMINISTRADOR -> PanelRolInfo(
        titulo = "Centro de Control General (Admin)",
        descripcion = "Supervisión total de usuarios, sistema y plataforma Ktor.",
        acciones = listOf(
            "Gestión centralizada de usuarios y asignación de roles.",
            "Monitoreo de estado de servicios Ktor y logs en tiempo real.",
            "Configuración de parámetros globales de la aplicación."
        ),
        metricasClave = listOf(
            "Usuarios Activos" to "142",
            "Servicios Ktor" to "Online (99.9%)",
            "Logs de Sistema" to "0 Errores"
        )
    )
    Rol.DOCENTE -> PanelRolInfo(
        titulo = "Panel de Gestión Académica (Docente)",
        descripcion = "Control de asignaturas, grupos y actividades de estudiantes.",
        acciones = listOf(
            "Creación y publicación de nuevas tareas y actividades.",
            "Revisión y calificación de evidencias enviadas.",
            "Seguimiento del rendimiento y promedio por grupo."
        ),
        metricasClave = listOf(
            "Grupos Asignados" to "4 Grupos",
            "Estudiantes" to "98 Totales",
            "Por Calificar" to "5 Evidencias"
        )
    )
    Rol.ESTUDIANTE -> PanelRolInfo(
        titulo = "Panel de Rendimiento Estudiantil",
        descripcion = "Planifica tu tiempo, cumple tus entregas y mejora tus hábitos.",
        acciones = listOf(
            "Registro de sesiones de estudio concentrado (Pomodoro).",
            "Entrega y actualización de tareas pendientes.",
            "Monitoreo de tu racha diaria y progreso académico."
        ),
        metricasClave = listOf(
            "Asignaturas" to "5 Materias",
            "Promedio General" to "9.4 / 10",
            "Entregas a Tiempo" to "96%"
        )
    )
}

private fun Rol.etiqueta(): String = when (this) {
    Rol.ADMINISTRADOR -> "Administrador"
    Rol.DOCENTE -> "Docente"
    Rol.ESTUDIANTE -> "Estudiante"
}

@Composable
fun MainScreen(
    usuario: LoginResponse,
    onCerrarSesion: () -> Unit
) {
    var tabActual by remember { mutableStateOf(MainTab.INICIO) }
    var mostrarDialogoNuevaTarea by remember { mutableStateOf(false) }
    var mostrarDialogoSesionEstudio by remember { mutableStateOf(false) }
    var mensajeSnack by remember { mutableStateOf<String?>(null) }

    val studyViewModel = remember { StudyViewModel() }

    var listaTareas by remember {
        mutableStateOf(
            listOf(
                TareaUI(1, "Proyecto Final KMP", "Programación Móvil", "Hoy, 23:59", "Alta"),
                TareaUI(2, "Resolución de Ejercicios Ch. 4", "Matemáticas", "Mañana, 18:00", "Media"),
                TareaUI(3, "Lectura de Arquitectura MVVM", "Ingeniería de Software", "15 Oct", "Baja"),
                TareaUI(4, "Preparar Presentación de Tesis", "Seminario", "18 Oct", "Alta")
            )
        )
    }

    val listaActividades = remember {
        listOf(
            ActividadDiaUI(1, "08:00 - 09:30", "Clase de Programación Kotlin", "Clase", "Completado"),
            ActividadDiaUI(2, "10:00 - 11:30", "Sesión Enfoque: Proyecto FocusUp", "Estudio", "En progreso"),
            ActividadDiaUI(3, "14:00 - 15:30", "Entrega de Avance de Red", "Entrega", "Próximo"),
            ActividadDiaUI(4, "16:00 - 17:30", "Repaso General de Algoritmos", "Estudio", "Próximo")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FocusUpColors.FondoOscuro,
                        FocusUpColors.Fondo,
                        Color(0xFF080D18)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
        ) {
            HeaderFuturista(usuario = usuario)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = tabActual, label = "cambio_tab") { tab ->
                    when (tab) {
                        MainTab.INICIO -> DashboardTabContent(
                            usuario = usuario,
                            tareas = listaTareas,
                            actividades = listaActividades,
                            onToggleTarea = { id ->
                                listaTareas = listaTareas.map {
                                    if (it.id == id) it.copy(completada = !it.completada) else it
                                }
                            },
                            onIniciarEstudio = { tabActual = MainTab.ESTUDIO },
                            onNuevaTarea = { mostrarDialogoNuevaTarea = true },
                            onVerEstadisticas = { tabActual = MainTab.ESTUDIO },
                            onSubirEvidencia = { tabActual = MainTab.ESTUDIO }
                        )

                        MainTab.ESTUDIO -> StudyScreen(
                            usuario = usuario,
                            viewModel = studyViewModel
                        )

                        MainTab.TAREAS -> TasksScreen(usuario = usuario)

                        MainTab.PERFIL -> ModuloPerfilContent(
                            usuario = usuario,
                            onCerrarSesion = onCerrarSesion
                        )
                    }
                }
            }

            NavegacionInferiorFuturista(
                tabSeleccionado = tabActual,
                onTabSeleccionado = { tabActual = it }
            )
        }

        mensajeSnack?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = FocusUpColors.TarjetaElevada,
                border = BordeTarjetaNeon
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡 ", fontSize = 16.sp)
                    Text(
                        text = msg,
                        color = FocusUpColors.Texto,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(onClick = { mensajeSnack = null }) {
                        Text("OK", color = FocusUpColors.AzulElectrico)
                    }
                }
            }
        }

        if (mostrarDialogoNuevaTarea) {
            DialogoNuevaTarea(
                onDesestimar = { mostrarDialogoNuevaTarea = false },
                onGuardar = { titulo, materia, prioridad ->
                    val nueva = TareaUI(
                        id = (listaTareas.maxOfOrNull { it.id } ?: 0) + 1,
                        titulo = titulo,
                        materia = materia,
                        fechaEntrega = "Próximamente",
                        prioridad = prioridad
                    )
                    listaTareas = listOf(nueva) + listaTareas
                    mostrarDialogoNuevaTarea = false
                    mensajeSnack = "Tarea \"$titulo\" creada con éxito."
                }
            )
        }

        if (mostrarDialogoSesionEstudio) {
            DialogoIniciarEstudio(
                onDesestimar = { mostrarDialogoSesionEstudio = false },
                onComenzar = { min, materia ->
                    mostrarDialogoSesionEstudio = false
                    tabActual = MainTab.ESTUDIO
                    mensajeSnack = "Sesión de $min min activada para $materia."
                }
            )
        }
    }
}

@Composable
private fun HeaderFuturista(usuario: LoginResponse) {
    val saludo = remember {
        val hora = 12
        when {
            hora < 12 -> "¡Buenos días!"
            hora < 19 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(FocusUpColors.AzulElectrico, FocusUpColors.PurpuraNeon, FocusUpColors.AzulElectrico)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(FocusUpColors.TarjetaElevada),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FocusUpColors.AzulElectrico
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = saludo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusUpColors.TextoSecundario
                    )
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusUpColors.Texto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FocusUpColors.TarjetaElevada,
                border = BorderStroke(1.dp, FocusUpColors.AzulElectrico.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FocusUpColors.AzulElectrico)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = usuario.rol.etiqueta(),
                        style = MaterialTheme.typography.labelMedium,
                        color = FocusUpColors.AzulElectrico,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTabContent(
    usuario: LoginResponse,
    tareas: List<TareaUI>,
    actividades: List<ActividadDiaUI>,
    onToggleTarea: (Int) -> Unit,
    onIniciarEstudio: () -> Unit,
    onNuevaTarea: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onSubirEvidencia: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
            border = BordeTarjetaNeon
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FocusUpColors.VerdeExito)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ESTADO DE ENFOQUE • ACTIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusUpColors.AzulElectrico,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Bienvenido a tu estación de trabajo, ${usuario.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusUpColors.Texto,
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FocusUpColors.AzulElectrico.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "87% Meta",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusUpColors.AzulElectrico,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Resumen de Productividad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FocusUpColors.Texto,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GridProductividad()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Accesos Rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FocusUpColors.Texto
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccesoRapidoCard(
                titulo = "Estudiar",
                icono = "🚀",
                subtitulo = "Iniciar Pomodoro",
                gradient = Brush.horizontalGradient(listOf(FocusUpColors.AzulElectrico, FocusUpColors.AzulNeon)),
                modifier = Modifier.weight(1f),
                onClick = onIniciarEstudio
            )

            AccesoRapidoCard(
                titulo = "Nueva Tarea",
                icono = "➕",
                subtitulo = "Crear pendiente",
                gradient = Brush.horizontalGradient(listOf(FocusUpColors.PurpuraNeon, FocusUpColors.RosaNeon)),
                modifier = Modifier.weight(1f),
                onClick = onNuevaTarea
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccesoRapidoCard(
                titulo = "Estadísticas",
                icono = "📊",
                subtitulo = "Ver progreso",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                modifier = Modifier.weight(1f),
                onClick = onVerEstadisticas
            )

            AccesoRapidoCard(
                titulo = "Evidencia",
                icono = "📷",
                subtitulo = "Subir foto",
                gradient = Brush.horizontalGradient(listOf(FocusUpColors.Ambar, FocusUpColors.Naranja)),
                modifier = Modifier.weight(1f),
                onClick = onSubirEvidencia
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tareas Pendientes (${tareas.count { !it.completada }})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto
            )

            TextButton(onClick = onNuevaTarea) {
                Text("+ Agregar", color = FocusUpColors.AzulElectrico, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        tareas.take(3).forEach { tarea ->
            ItemTareaFuturista(tarea = tarea, onToggle = { onToggleTarea(tarea.id) })
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Actividades de Hoy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FocusUpColors.Texto
        )

        Spacer(modifier = Modifier.height(12.dp))

        actividades.forEach { actividad ->
            ItemActividadDia(actividad = actividad)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun GridProductividad() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaCard(
                titulo = "Tiempo Hoy",
                valor = "3.5 hrs",
                subtitulo = "Meta: 4.0 hrs (87%)",
                icono = "⏱️",
                color = FocusUpColors.AzulElectrico,
                modifier = Modifier.weight(1f)
            )

            MetricaCard(
                titulo = "Score Enfoque",
                valor = "94%",
                subtitulo = "Nivel Excelente",
                icono = "🎯",
                color = FocusUpColors.VerdeExito,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaCard(
                titulo = "Racha Actual",
                valor = "7 Días",
                subtitulo = "¡En fuego! 🔥",
                icono = "⚡",
                color = FocusUpColors.Ambar,
                modifier = Modifier.weight(1f)
            )

            MetricaCard(
                titulo = "Completadas",
                valor = "5 / 7",
                subtitulo = "Tareas del día",
                icono = "✅",
                color = FocusUpColors.PurpuraNeon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricaCard(
    titulo: String,
    valor: String,
    subtitulo: String,
    icono: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, style = MaterialTheme.typography.labelMedium, color = FocusUpColors.TextoSecundario)
                Text(text = icono, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusUpColors.Texto
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    icono: String,
    subtitulo: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icono, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitulo,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemTareaFuturista(
    tarea: TareaUI,
    onToggle: () -> Unit
) {
    val colorPrioridad = when (tarea.prioridad) {
        "Alta" -> FocusUpColors.Error
        "Media" -> FocusUpColors.Ambar
        else -> FocusUpColors.AzulElectrico
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) FocusUpColors.Tarjeta.copy(alpha = 0.5f) else FocusUpColors.Tarjeta
        ),
        border = BorderStroke(1.dp, if (tarea.completada) FocusUpColors.Borde else colorPrioridad.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = FocusUpColors.AzulElectrico,
                    uncheckedColor = FocusUpColors.BordeCampo
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.titulo,
                    fontWeight = FontWeight.Bold,
                    color = if (tarea.completada) FocusUpColors.TextoSecundario else FocusUpColors.Texto,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tarea.materia,
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusUpColors.TextoSecundario
                    )
                    Text(text = " • ", color = FocusUpColors.TextoSecundario)
                    Text(
                        text = tarea.fechaEntrega,
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusUpColors.TextoSecundario
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorPrioridad.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, colorPrioridad.copy(alpha = 0.4f))
            ) {
                Text(
                    text = tarea.prioridad,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorPrioridad,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ItemActividadDia(actividad: ActividadDiaUI) {
    val (colorEstado, labelEstado) = when (actividad.estado) {
        "Completado" -> FocusUpColors.VerdeExito to "✓ Hecho"
        "En progreso" -> FocusUpColors.AzulElectrico to "⚡ En Curso"
        else -> FocusUpColors.TextoSecundario to "⏳ Próximo"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
        border = BordeTarjeta
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(colorEstado)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actividad.titulo,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusUpColors.Texto,
                    fontSize = 14.sp
                )
                Text(
                    text = "${actividad.hora} • ${actividad.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusUpColors.TextoSecundario
                )
            }

            Text(
                text = labelEstado,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorEstado
            )
        }
    }
}

@Composable
private fun ModuloPerfilContent(
    usuario: LoginResponse,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(FocusUpColors.AzulElectrico, FocusUpColors.PurpuraNeon, FocusUpColors.AzulElectrico)
                    )
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(FocusUpColors.TarjetaElevada),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nombre.take(1).uppercase(),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusUpColors.AzulElectrico
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = usuario.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FocusUpColors.Texto
        )

        Text(
            text = usuario.correo,
            style = MaterialTheme.typography.bodyMedium,
            color = FocusUpColors.TextoSecundario
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FocusUpColors.AzulElectrico.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, FocusUpColors.AzulElectrico.copy(alpha = 0.4f))
        ) {
            Text(
                text = usuario.rol.etiqueta(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = FocusUpColors.AzulElectrico,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val panelInfo = remember(usuario.rol) { panelPara(usuario.rol) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
            border = BordeTarjetaNeon
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = panelInfo.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusUpColors.Texto
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = panelInfo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusUpColors.TextoSecundario
                )

                Spacer(modifier = Modifier.height(16.dp))

                panelInfo.metricasClave.forEach { (label, valStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = FocusUpColors.TextoSecundario, fontSize = 13.sp)
                        Text(text = valStr, color = FocusUpColors.AzulElectrico, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.Error)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NavegacionInferiorFuturista(
    tabSeleccionado: MainTab,
    onTabSeleccionado: (MainTab) -> Unit
) {
    Surface(
        color = FocusUpColors.TarjetaElevada,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, FocusUpColors.Borde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val seleccionado = tab == tabSeleccionado
                val colorIcono by animateColorAsState(
                    if (seleccionado) FocusUpColors.AzulElectrico else FocusUpColors.TextoSecundario,
                    label = "color_tab"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSeleccionado(tab) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = tab.icono, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.titulo,
                        fontSize = 11.sp,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                        color = colorIcono
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevaTarea(
    onDesestimar: () -> Unit,
    onGuardar: (titulo: String, materia: String, prioridad: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var materia by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("Media") }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = { Text("Nueva Tarea", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la tarea") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = materia,
                    onValueChange = { materia = it },
                    label = { Text("Materia / Asignatura") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titulo.isNotBlank()) onGuardar(titulo, materia, prioridad) },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Guardar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDesestimar) {
                Text("Cancelar", color = FocusUpColors.TextoSecundario)
            }
        }
    )
}

@Composable
private fun DialogoIniciarEstudio(
    onDesestimar: () -> Unit,
    onComenzar: (minutos: Int, materia: String) -> Unit
) {
    var minutos by remember { mutableStateOf(25) }
    var materia by remember { mutableStateOf("Programación Kotlin") }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = { Text("Iniciar Enfoque Pomodoro", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Selecciona la duración deseada para tu sesión:", style = MaterialTheme.typography.bodyMedium, color = FocusUpColors.TextoSecundario)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 25, 45, 60).forEach { m ->
                        FilterChip(
                            selected = minutos == m,
                            onClick = { minutos = m },
                            label = { Text("${m}m") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onComenzar(minutos, materia) },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Iniciar Pomodoro", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDesestimar) {
                Text("Cancelar", color = FocusUpColors.TextoSecundario)
            }
        }
    )
}