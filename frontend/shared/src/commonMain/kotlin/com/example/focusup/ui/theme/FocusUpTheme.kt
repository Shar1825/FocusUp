package com.example.focusup.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Paleta de FocusUp, tomada de la presentación del proyecto.
 * Contrastes verificados (WCAG AA, mínimo 4.5:1 para texto).
 */
internal object FocusUpColors {
    val Fondo = Color(0xFF090D16)
    val FondoOscuro = Color(0xFF05070B)
    val Tarjeta = Color(0xFF131A27)
    val TarjetaElevada = Color(0xFF1C2436)
    val Borde = Color(0xFF26334A)             // contorno decorativo de tarjetas
    val BordeCampo = Color(0xFF475569)        // contorno de campos

    val Texto = Color(0xFFF8FAFC)
    val TextoSecundario = Color(0xFF94A3B8)

    val Ambar = Color(0xFFF5B342)             // acento secundario / cálido
    val Naranja = Color(0xFFFF7A45)

    val AzulElectrico = Color(0xFF00F0FF)     // Neón futurista principal
    val AzulNeon = Color(0xFF2563EB)
    val AzulCyan = Color(0xFF38BDF8)
    val VerdeExito = Color(0xFF10B981)
    val PurpuraNeon = Color(0xFF8B5CF6)
    val RosaNeon = Color(0xFFEC4899)

    val Error = Color(0xFFFF8A5C)
    val ErrorFondo = Color(0xFF3A1E1E)
    val ErrorTexto = Color(0xFFFFB4A0)
}

/** Ancho máximo del formulario: mantiene el equilibrio en tablets y escritorio. */
internal val AnchoMaximoLogin = 480.dp

/** Contorno sutil de las tarjetas, igual que en la presentación. */
internal val BordeTarjeta = BorderStroke(1.dp, FocusUpColors.Borde)
internal val BordeTarjetaNeon = BorderStroke(1.dp, FocusUpColors.AzulElectrico.copy(alpha = 0.35f))

/** Tema global de la app. Envuelve toda la UI en App.kt. */
@Composable
fun FocusUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = FocusUpColors.Ambar,
            onPrimary = FocusUpColors.Fondo,
            primaryContainer = FocusUpColors.TarjetaElevada,
            onPrimaryContainer = FocusUpColors.Texto,
            secondary = FocusUpColors.Naranja,
            onSecondary = FocusUpColors.Fondo,
            secondaryContainer = FocusUpColors.TarjetaElevada,
            onSecondaryContainer = FocusUpColors.Texto,
            background = FocusUpColors.Fondo,
            onBackground = FocusUpColors.Texto,
            surface = FocusUpColors.Tarjeta,
            onSurface = FocusUpColors.Texto,
            surfaceVariant = FocusUpColors.TarjetaElevada,
            onSurfaceVariant = FocusUpColors.TextoSecundario,
            outline = FocusUpColors.BordeCampo,
            error = FocusUpColors.Error,
            onError = FocusUpColors.Fondo,
            errorContainer = FocusUpColors.ErrorFondo,
            onErrorContainer = FocusUpColors.ErrorTexto
        ),
        content = content
    )
}