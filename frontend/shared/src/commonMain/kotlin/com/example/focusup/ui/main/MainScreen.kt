package com.example.focusup.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
enum class MainTab {
    INICIO,
    ESTUDIO,
    TAREAS,
    USUARIOS,
    PERFIL
}

private data class TabInfo(
    val tab: MainTab,
    val titulo: String,
    val icono: String
)

private fun tabsParaRol(rol: Rol): List<TabInfo> = when (rol) {
    Rol.ESTUDIANTE -> listOf(
        TabInfo(MainTab.INICIO, "Inicio", "🏠"),
        TabInfo(MainTab.ESTUDIO, "Estudio", "⏱️"),
        TabInfo(MainTab.TAREAS, "Tareas", "📋"),
        TabInfo(MainTab.PERFIL, "Perfil", "👤")
    )
    Rol.DOCENTE -> listOf(
        TabInfo(MainTab.INICIO, "Inicio", "🏠"),
        TabInfo(MainTab.ESTUDIO, "Validación", "⏱️"),
        TabInfo(MainTab.TAREAS, "Actividades", "📋"),
        TabInfo(MainTab.PERFIL, "Perfil", "👤")
    )
    Rol.ADMINISTRADOR -> listOf(
        TabInfo(MainTab.INICIO, "Inicio", "🏠"),
        TabInfo(MainTab.USUARIOS, "Usuarios", "👥"),
        TabInfo(MainTab.PERFIL, "Perfil", "👤")
    )
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
    var mensajeSnack by remember { mutableStateOf<String?>(null) }

    val studyViewModel = remember { StudyViewModel() }
    val dashboardViewModel = remember { DashboardViewModel() }
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    LaunchedEffect(usuario.rol) {
        dashboardViewModel.cargarDatos(usuario.rol)
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
                            state = dashboardState,
                            onReintentar = { dashboardViewModel.cargarDatos(usuario.rol) },
                            onIniciarEstudio = { tabActual = MainTab.ESTUDIO },
                            onNuevaTarea = {
                                if (usuario.rol == Rol.DOCENTE) {
                                    mostrarDialogoNuevaTarea = true
                                }
                            },
                            onVerEstadisticas = { tabActual = MainTab.PERFIL },
                            onSubirEvidencia = { tabActual = MainTab.ESTUDIO },
                            onGestionUsuarios = { tabActual = MainTab.USUARIOS }
                        )

                        MainTab.ESTUDIO -> StudyScreen(
                            usuario = usuario,
                            viewModel = studyViewModel
                        )

                        MainTab.TAREAS -> TasksScreen(usuario = usuario)

                        MainTab.USUARIOS -> PantallaProvisionalUsuarios()

                        MainTab.PERFIL -> ModuloPerfilContent(
                            usuario = usuario,
                            state = dashboardState,
                            onReintentar = { dashboardViewModel.cargarDatos(usuario.rol) },
                            onCerrarSesion = onCerrarSesion
                        )
                    }
                }
            }

            NavegacionInferiorFuturista(
                usuario = usuario,
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
private fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_anim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(FocusUpColors.TarjetaElevada.copy(alpha = alpha))
    )
}

@Composable
private fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(20.dp)
        )

        SkeletonBox(
            modifier = Modifier
                .width(220.dp)
                .height(26.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(18.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(18.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(18.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(18.dp)
            )
        }

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun DashboardErrorView(
    error: String,
    onReintentar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
        border = BorderStroke(1.dp, FocusUpColors.Error.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⚠️", fontSize = 44.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Error de Conexión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = FocusUpColors.TextoSecundario,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusUpColors.AzulElectrico,
                    contentColor = FocusUpColors.Fondo
                )
            ) {
                Text(
                    text = "🔄 Reintentar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DashboardTabContent(
    usuario: LoginResponse,
    state: DashboardUiState,
    onReintentar: () -> Unit,
    onIniciarEstudio: () -> Unit,
    onNuevaTarea: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onSubirEvidencia: () -> Unit,
    onGestionUsuarios: () -> Unit
) {
    if (state.cargando) {
        DashboardSkeleton()
        return
    }

    if (state.error != null) {
        DashboardErrorView(error = state.error, onReintentar = onReintentar)
        return
    }

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

        GridProductividadReal(usuario = usuario, state = state)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Accesos Rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FocusUpColors.Texto
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (usuario.rol) {
            Rol.ESTUDIANTE -> {
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
                        titulo = "Mis Tareas",
                        icono = "📋",
                        subtitulo = "Ver pendientes",
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
                        titulo = "Mi Perfil",
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
            }

            Rol.DOCENTE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccesoRapidoCard(
                        titulo = "Validar",
                        icono = "⏱️",
                        subtitulo = "Revisar evidencias",
                        gradient = Brush.horizontalGradient(listOf(FocusUpColors.AzulElectrico, FocusUpColors.AzulNeon)),
                        modifier = Modifier.weight(1f),
                        onClick = onIniciarEstudio
                    )

                    AccesoRapidoCard(
                        titulo = "Nueva Actividad",
                        icono = "➕",
                        subtitulo = "Crear tarea",
                        gradient = Brush.horizontalGradient(listOf(FocusUpColors.PurpuraNeon, FocusUpColors.RosaNeon)),
                        modifier = Modifier.weight(1f),
                        onClick = onNuevaTarea
                    )
                }
            }

            Rol.ADMINISTRADOR -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccesoRapidoCard(
                        titulo = "Usuarios",
                        icono = "👥",
                        subtitulo = "Gestión central",
                        gradient = Brush.horizontalGradient(listOf(FocusUpColors.AzulElectrico, FocusUpColors.PurpuraNeon)),
                        modifier = Modifier.weight(1f),
                        onClick = onGestionUsuarios
                    )

                    AccesoRapidoCard(
                        titulo = "Perfil Admin",
                        icono = "👤",
                        subtitulo = "Configuración",
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                        modifier = Modifier.weight(1f),
                        onClick = onVerEstadisticas
                    )
                }
            }
        }

        if (usuario.rol == Rol.ESTUDIANTE && state.actividadesEstudiante.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Actividades Académicas (${state.actividadesEstudiante.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.actividadesEstudiante.take(4).forEach { act ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                        Text("📋 ", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = act.titulo,
                                fontWeight = FontWeight.Bold,
                                color = FocusUpColors.Texto,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (act.fechaEntrega.isNotBlank()) "Entrega: ${act.fechaEntrega}" else "Sin fecha asignada",
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusUpColors.TextoSecundario
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun GridProductividadReal(
    usuario: LoginResponse,
    state: DashboardUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (usuario.rol) {
            Rol.ESTUDIANTE -> {
                val prog = state.studentProgress
                val minHoy = prog?.minutosEstudioCalculados ?: 0
                val promedio = prog?.promedioCalificaciones ?: 0.0
                val racha = prog?.rachaDiasCalculada ?: 0
                val pend = prog?.actividadesPendientes ?: 0
                val comp = prog?.actividadesCompletadas ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Minutos Estudio",
                        valor = "$minHoy min",
                        subtitulo = "Sesiones del día",
                        icono = "⏱️",
                        color = FocusUpColors.AzulElectrico,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Promedio General",
                        valor = "$promedio",
                        subtitulo = "Calificaciones",
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
                        titulo = "Racha Validad",
                        valor = "$racha Días",
                        subtitulo = "Días seguidos",
                        icono = "⚡",
                        color = FocusUpColors.Ambar,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Tareas Pendientes",
                        valor = "$pend",
                        subtitulo = "$comp completadas",
                        icono = "✅",
                        color = FocusUpColors.PurpuraNeon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Rol.DOCENTE -> {
                val prof = state.teacherProfile
                val grupos = prof?.cantidadGrupos ?: 0
                val estudiantes = prof?.cantidadEstudiantes ?: 0
                val porCalificar = prof?.cantidadEntregasPendientes ?: 0
                val evidencias = prof?.cantidadEvidenciasPendientes ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Grupos Asignados",
                        valor = "$grupos",
                        subtitulo = "Grupos a cargo",
                        icono = "🏫",
                        color = FocusUpColors.AzulElectrico,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Total Estudiantes",
                        valor = "$estudiantes",
                        subtitulo = "Estudiantes",
                        icono = "👥",
                        color = FocusUpColors.VerdeExito,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Por Calificar",
                        valor = "$porCalificar",
                        subtitulo = "Entregas pendientes",
                        icono = "📝",
                        color = FocusUpColors.Ambar,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Evidencias Pendientes",
                        valor = "$evidencias",
                        subtitulo = "Por validar",
                        icono = "📷",
                        color = FocusUpColors.PurpuraNeon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Rol.ADMINISTRADOR -> {
                val stats = state.adminStatistics
                val totalActivos = stats?.totalUsuariosActivos ?: 0
                val estCount = stats?.estudiantesCount ?: 0
                val docCount = stats?.docentesCount ?: 0
                val sesiones = stats?.totalSesionesEstudio ?: 0
                val evPend = stats?.totalEvidenciasPendientes ?: 0
                val evAprob = stats?.totalEvidenciasAprobadas ?: 0
                val evRech = stats?.totalEvidenciasRechazadas ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Usuarios Activos",
                        valor = "$totalActivos",
                        subtitulo = "$estCount Est | $docCount Doc",
                        icono = "👥",
                        color = FocusUpColors.AzulElectrico,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Sesiones Estudio",
                        valor = "$sesiones",
                        subtitulo = "Total sesiones",
                        icono = "⏱️",
                        color = FocusUpColors.VerdeExito,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Evidencias Pendientes",
                        valor = "$evPend",
                        subtitulo = "Pendientes",
                        icono = "⏳",
                        color = FocusUpColors.Ambar,
                        modifier = Modifier.weight(1f)
                    )

                    MetricaCard(
                        titulo = "Evidencias Validadas",
                        valor = "$evAprob",
                        subtitulo = "$evRech rechazadas",
                        icono = "✅",
                        color = FocusUpColors.PurpuraNeon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
private fun PantallaProvisionalUsuarios() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
            border = BordeTarjetaNeon
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "👥", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gestión de Usuarios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FocusUpColors.Texto,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Próximamente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = FocusUpColors.AzulElectrico,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Módulo en desarrollo para la administración centralizada de cuentas de estudiantes y docentes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusUpColors.TextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModuloPerfilContent(
    usuario: LoginResponse,
    state: DashboardUiState,
    onReintentar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    if (state.cargando) {
        DashboardSkeleton()
        return
    }

    if (state.error != null) {
        DashboardErrorView(error = state.error, onReintentar = onReintentar)
        return
    }

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

        val metricas: List<Pair<String, String>> = remember(usuario.rol, state) {
            when (usuario.rol) {
                Rol.ESTUDIANTE -> {
                    val prog = state.studentProgress
                    listOf(
                        "Tareas Pendientes" to "${prog?.actividadesPendientes ?: 0}",
                        "Tareas Entregadas" to "${prog?.actividadesCompletadas ?: 0}",
                        "Promedio General" to "${prog?.promedioCalificaciones ?: 0.0}",
                        "Minutos de Estudio Hoy" to "${prog?.minutosEstudioCalculados ?: 0} min",
                        "Racha de Días Validados" to "${prog?.rachaDiasCalculada ?: 0} días"
                    )
                }
                Rol.DOCENTE -> {
                    val prof = state.teacherProfile
                    listOf(
                        "Grupos Asignados" to "${prof?.cantidadGrupos ?: 0}",
                        "Total Estudiantes" to "${prof?.cantidadEstudiantes ?: 0}",
                        "Entregas por Calificar" to "${prof?.cantidadEntregasPendientes ?: 0}",
                        "Evidencias Pendientes" to "${prof?.cantidadEvidenciasPendientes ?: 0}"
                    )
                }
                Rol.ADMINISTRADOR -> {
                    val stats = state.adminStatistics
                    listOf(
                        "Total Usuarios Activos" to "${stats?.totalUsuariosActivos ?: 0}",
                        "Estudiantes Activos" to "${stats?.estudiantesCount ?: 0}",
                        "Docentes Activos" to "${stats?.docentesCount ?: 0}",
                        "Administradores Activos" to "${stats?.administradoresCount ?: 0}",
                        "Sesiones de Estudio" to "${stats?.totalSesionesEstudio ?: 0}",
                        "Evidencias Pendientes" to "${stats?.totalEvidenciasPendientes ?: 0}",
                        "Evidencias Aprobadas" to "${stats?.totalEvidenciasAprobadas ?: 0}",
                        "Evidencias Rechazadas" to "${stats?.totalEvidenciasRechazadas ?: 0}"
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
            border = BordeTarjetaNeon
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Métricas Reales del Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusUpColors.Texto
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Datos en tiempo real obtenidos desde la API Ktor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusUpColors.TextoSecundario
                )

                Spacer(modifier = Modifier.height(16.dp))

                metricas.forEach { (label, valStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
    usuario: LoginResponse,
    tabSeleccionado: MainTab,
    onTabSeleccionado: (MainTab) -> Unit
) {
    val tabs = remember(usuario.rol) { tabsParaRol(usuario.rol) }

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
            tabs.forEach { item ->
                val seleccionado = item.tab == tabSeleccionado
                val colorIcono by animateColorAsState(
                    if (seleccionado) FocusUpColors.AzulElectrico else FocusUpColors.TextoSecundario,
                    label = "color_tab"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSeleccionado(item.tab) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = item.icono, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.titulo,
                        fontSize = 11.sp,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                        color = colorIcono
                    )
                }
            }
        }
    }
}
