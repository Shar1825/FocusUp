package com.example.focusup.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.ui.theme.AnchoMaximoLogin
import com.example.focusup.ui.theme.BordeTarjetaNeon
import com.example.focusup.ui.theme.FocusUpColors

/**
 * Pantalla de inicio de sesión futurista para FocusUp.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleVerPassword: () -> Unit,
    onIniciarSesion: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        FocusUpColors.FondoOscuro,
                        FocusUpColors.Fondo,
                        Color(0xFF070B14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = AnchoMaximoLogin)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BordeTarjetaNeon,
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = FocusUpColors.TarjetaElevada)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 36.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Neón Futurista
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(FocusUpColors.AzulElectrico, FocusUpColors.PurpuraNeon, FocusUpColors.AzulElectrico)
                                )
                            )
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(FocusUpColors.FondoOscuro)
                            .clearAndSetSemantics { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FU",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = FocusUpColors.AzulElectrico
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("Focus")
                            withStyle(SpanStyle(color = FocusUpColors.AzulElectrico)) { append("Up") }
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = FocusUpColors.Texto,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Gestión Académica & Enfoque Neón",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FocusUpColors.AzulCyan,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingresa con tu cuenta de Administrador, Docente o Estudiante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusUpColors.TextoSecundario,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Chips de Selección Rápida para Pruebas / Demostración
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickUserChip("Estudiante", "estudiante@focusup.com", onCorreoChange, onPasswordChange)
                        QuickUserChip("Docente", "docente@focusup.com", onCorreoChange, onPasswordChange)
                        QuickUserChip("Admin", "admin@focusup.com", onCorreoChange, onPasswordChange)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo Correo
                    OutlinedTextField(
                        value = state.correo,
                        onValueChange = onCorreoChange,
                        label = { Text("Correo electrónico", color = FocusUpColors.TextoSecundario) },
                        placeholder = { Text("ejemplo@focusup.com", color = FocusUpColors.BordeCampo) },
                        singleLine = true,
                        isError = state.error != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusUpColors.AzulElectrico,
                            unfocusedBorderColor = FocusUpColors.BordeCampo,
                            focusedTextColor = FocusUpColors.Texto,
                            unfocusedTextColor = FocusUpColors.Texto
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña", color = FocusUpColors.TextoSecundario) },
                        singleLine = true,
                        isError = state.error != null,
                        visualTransformation = if (state.verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = onToggleVerPassword) {
                                Text(
                                    if (state.verPassword) "Ocultar" else "Mostrar",
                                    color = FocusUpColors.AzulElectrico,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusUpColors.AzulElectrico,
                            unfocusedBorderColor = FocusUpColors.BordeCampo,
                            focusedTextColor = FocusUpColors.Texto,
                            unfocusedTextColor = FocusUpColors.Texto
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onIniciarSesion() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje de Error
                    state.error?.let { error ->
                        Surface(
                            color = FocusUpColors.ErrorFondo,
                            border = BorderStroke(1.dp, FocusUpColors.Error),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = error,
                                    color = FocusUpColors.ErrorTexto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Botón Iniciar Sesión
                    Button(
                        onClick = onIniciarSesion,
                        enabled = !state.cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusUpColors.AzulElectrico,
                            contentColor = FocusUpColors.Fondo,
                            disabledContainerColor = FocusUpColors.AzulElectrico.copy(alpha = 0.4f)
                        )
                    ) {
                        if (state.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .semantics { contentDescription = "Iniciando sesión" },
                                color = FocusUpColors.Fondo,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Iniciar sesión ⚡",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickUserChip(
    etiqueta: String,
    correo: String,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable {
                onCorreoChange(correo)
                onPasswordChange("123456")
            },
        shape = RoundedCornerShape(20.dp),
        color = FocusUpColors.Tarjeta,
        border = BorderStroke(1.dp, FocusUpColors.AzulElectrico.copy(alpha = 0.3f))
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = FocusUpColors.AzulElectrico,
            fontWeight = FontWeight.Bold
        )
    }
}
