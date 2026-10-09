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
    val Fondo = Color(0xFF0E1116)
    val Tarjeta = Color(0xFF1C2230)
    val TarjetaElevada = Color(0xFF252C3B)
    val Borde = Color(0xFF2B3244)             // contorno decorativo de tarjetas
    val BordeCampo = Color(0xFF7D879A)        // contorno de campos (4.4:1, cumple 3:1)

    val Texto = Color(0xFFF2EFE9)
    val TextoSecundario = Color(0xFF9AA3B2)

    val Ambar = Color(0xFFF5B342)             // acento principal
    val Naranja = Color(0xFFFF7A45)           // acento secundario

    val Error = Color(0xFFFF8A5C)
    val ErrorFondo = Color(0xFF3A2A1A)
    val ErrorTexto = Color(0xFFFFB4A0)
}

/** Ancho máximo del formulario: mantiene el equilibrio en tablets y escritorio. */
internal val AnchoMaximoLogin = 480.dp

/** Contorno sutil de las tarjetas, igual que en la presentación. */
internal val BordeTarjeta = BorderStroke(1.dp, FocusUpColors.Borde)

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