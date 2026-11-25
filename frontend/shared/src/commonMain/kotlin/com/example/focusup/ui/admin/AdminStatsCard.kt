package com.example.focusup.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.AdminStatistics
import com.example.focusup.ui.theme.FocusUpColors

/**
 * Componente reutilizable en commonMain para visualizar métricas y estadísticas administrativas.
 */
@Composable
fun AdminStatsCard(
    stats: AdminStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada),
        border = BorderStroke(1.dp, FocusUpColors.AzulElectrico.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📊 Estadísticas Globales del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusUpColors.Texto
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Métricas Principales de Usuarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Total Usuarios", valor = "${stats.totalUsuarios.takeIf { it > 0 } ?: (stats.cantidadAdmin + stats.cantidadDocentes + stats.cantidadEstudiantes)}", color = FocusUpColors.AzulElectrico)
                StatItem(label = "Activos", valor = "${stats.usuariosActivos.takeIf { it > 0 } ?: stats.totalUsuarios}", color = FocusUpColors.VerdeExito)
                StatItem(label = "Inactivos", valor = "${stats.usuariosInactivos}", color = FocusUpColors.Error)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = FocusUpColors.BordeCampo.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Desglose por Rol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Estudiantes", valor = "${stats.cantidadEstudiantes.takeIf { it > 0 } ?: stats.estudiantesCount}", color = FocusUpColors.AzulCyan)
                StatItem(label = "Docentes", valor = "${stats.cantidadDocentes.takeIf { it > 0 } ?: stats.docentesCount}", color = FocusUpColors.PurpuraNeon)
                StatItem(label = "Admins", valor = "${stats.cantidadAdmin.takeIf { it > 0 } ?: stats.administradoresCount}", color = FocusUpColors.Ambar)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = FocusUpColors.BordeCampo.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Métricas Académicas y Pomodoro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Grupos", valor = "${stats.totalGrupos.takeIf { it > 0 } ?: stats.gruposTotales}", color = FocusUpColors.Texto)
                StatItem(label = "Actividades", valor = "${stats.totalActividades.takeIf { it > 0 } ?: stats.actividadesTotales}", color = FocusUpColors.Texto)
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    valor: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = FocusUpColors.TextoSecundario
        )
    }
}
