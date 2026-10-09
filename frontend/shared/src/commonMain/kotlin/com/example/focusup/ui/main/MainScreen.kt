package com.example.focusup.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.Rol
import com.example.focusup.ui.theme.BordeTarjeta

/** Contenido que cada rol ve en su panel (evita tres composables casi idénticos). */
private data class PanelRol(
    val titulo: String,
    val descripcion: String,
    val acciones: List<String>
)

/** Navegación por rol: devuelve el panel que corresponde al rol autenticado. */
private fun panelPara(rol: Rol): PanelRol = when (rol) {
    Rol.ADMINISTRADOR -> PanelRol(
        titulo = "Módulo de administración",
        descripcion = "Controla el sistema y a sus usuarios.",
        acciones = listOf(
            "Gestión de usuarios y asignación de roles.",
            "Monitoreo de logs del servidor Ktor.",
            "Configuración general del sistema FocusUp."
        )
    )
    Rol.DOCENTE -> PanelRol(
        titulo = "Módulo de docencia",
        descripcion = "Organiza tus grupos y actividades.",
        acciones = listOf(
            "Creación y gestión de asignaturas.",
            "Publicación de tareas y actividades.",
            "Registro de calificaciones de estudiantes."
        )
    )
    Rol.ESTUDIANTE -> PanelRol(
        titulo = "Módulo de estudiante",
        descripcion = "Mantén tus estudios al día.",
        acciones = listOf(
            "Consulta de cursos matriculados.",
            "Entrega de tareas y proyectos.",
            "Visualización de progreso académico."
        )
    )
}

/** Nombre legible del rol (Administrador, Docente, Estudiante). */
private fun Rol.etiqueta(): String = name.lowercase().replaceFirstChar { it.uppercase() }

/**
 * Pantalla principal tras autenticarse: perfil del usuario + panel según su rol.
 *
 * @param usuario Datos del usuario devueltos por la API Ktor.
 * @param onCerrarSesion Callback para cerrar la sesión (lo resuelve el ViewModel).
 */
@Composable
fun MainScreen(usuario: LoginResponse, onCerrarSesion: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BordeTarjeta,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clearAndSetSemantics { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = usuario.nombre.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuario.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = usuario.correo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text(
                            text = "Rol: ${usuario.rol.etiqueta()}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val panel = panelPara(usuario.rol)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BordeTarjeta,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = panel.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = panel.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        panel.acciones.forEach { accion ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clearAndSetSemantics { }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = accion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}