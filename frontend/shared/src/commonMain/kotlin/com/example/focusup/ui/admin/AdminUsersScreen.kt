package com.example.focusup.ui.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusup.model.CreateUserRequest
import com.example.focusup.model.LoginResponse
import com.example.focusup.model.Rol
import com.example.focusup.model.UpdateUserRequest
import com.example.focusup.model.UserDto
import com.example.focusup.network.AdminService
import com.example.focusup.ui.theme.BordeTarjeta
import com.example.focusup.ui.theme.BordeTarjetaNeon
import com.example.focusup.ui.theme.FocusUpColors
import kotlinx.coroutines.launch

/**
 * Pantalla reutilizable en commonMain para la gestión centralizada de Usuarios (Administrador).
 * Diseñada sin dependencias nativas de Android para su reutilización en Web, Desktop e iOS.
 */
@Composable
fun AdminUsersScreen(
    adminActual: LoginResponse,
    adminService: AdminService = remember { AdminService() }
) {
    val scope = rememberCoroutineScope()
    var listaUsuarios by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var filtroRol by remember { mutableStateOf("Todos") }

    var mostrarModalCrear by remember { mutableStateOf(false) }
    var usuarioEditar by remember { mutableStateOf<UserDto?>(null) }

    fun cargarUsuarios() {
        cargando = true
        error = null
        scope.launch {
            val rolEnum = when (filtroRol) {
                "Estudiante" -> Rol.ESTUDIANTE
                "Docente" -> Rol.DOCENTE
                "Administrador" -> Rol.ADMINISTRADOR
                else -> null
            }
            val res = adminService.getUsers(query = searchQuery.ifBlank { null }, rol = rolEnum)
            cargando = false
            res.onSuccess { lista ->
                listaUsuarios = lista
            }.onFailure { ex ->
                error = "Error al consultar usuarios: ${ex.message}"
            }
        }
    }

    LaunchedEffect(searchQuery, filtroRol) {
        cargarUsuarios()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Cabecera del Módulo de Administración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gestión de Usuarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FocusUpColors.Texto,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Administra las cuentas de estudiantes, docentes y administradores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusUpColors.TextoSecundario
                    )
                }

                IconButton(
                    onClick = { mostrarModalCrear = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FocusUpColors.AzulElectrico)
                ) {
                    Text("➕", color = FocusUpColors.Fondo, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de búsqueda por nombre o correo
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre o correo...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FocusUpColors.AzulElectrico,
                    unfocusedBorderColor = FocusUpColors.BordeCampo
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Rol
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Todos", "Estudiante", "Docente", "Administrador")) { rolName ->
                    val sel = filtroRol == rolName
                    FilterChip(
                        selected = sel,
                        onClick = { filtroRol = rolName },
                        label = { Text(rolName, fontWeight = FontWeight.Bold) },
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

            // Estados de Carga, Error y Lista Vacía
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FocusUpColors.AzulElectrico)
                }
            } else if (error != null && listaUsuarios.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
                    border = BorderStroke(1.dp, FocusUpColors.Error)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️ Sin conexión con el servidor", fontWeight = FontWeight.Bold, color = FocusUpColors.ErrorTexto)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = error ?: "No se pudieron obtener los usuarios.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusUpColors.TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { cargarUsuarios() },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
                        ) {
                            Text("🔄 Reintentar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (listaUsuarios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron usuarios coincidentes.",
                        color = FocusUpColors.TextoSecundario,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaUsuarios, key = { it.id }) { u ->
                        ItemUsuarioAdmin(
                            usuarioItem = u,
                            esAdminActual = u.id == adminActual.id,
                            onEditar = { usuarioEditar = u },
                            onToggleStatus = { nuevoEstado ->
                                scope.launch {
                                    adminService.updateUserStatus(u.id, nuevoEstado)
                                        .onSuccess { act ->
                                            listaUsuarios = listaUsuarios.map { if (it.id == act.id) act else it }
                                            mensaje = "Estado de ${act.nombre} actualizado a ${if (act.activo) "Activo" else "Inactivo"}."
                                        }
                                        .onFailure { ex ->
                                            error = "No se pudo actualizar estado: ${ex.message}"
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Notificaciones / SnackBar de éxito o error
        mensaje?.let { msg ->
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
                    TextButton(onClick = { mensaje = null }) {
                        Text("OK", color = FocusUpColors.AzulElectrico)
                    }
                }
            }
        }

        error?.let { err ->
            if (listaUsuarios.isNotEmpty()) {
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
                        TextButton(onClick = { error = null }) {
                            Text("Cerrar", color = FocusUpColors.Texto)
                        }
                    }
                }
            }
        }
    }

    // Modal Crear Usuario
    if (mostrarModalCrear) {
        ModalCrearUsuarioAdmin(
            onDesestimar = { mostrarModalCrear = false },
            onGuardar = { req ->
                scope.launch {
                    adminService.createUser(req)
                        .onSuccess { nuevo ->
                            mostrarModalCrear = false
                            mensaje = "Usuario ${nuevo.nombre} registrado con éxito."
                            cargarUsuarios()
                        }
                        .onFailure { ex ->
                            error = "Error al registrar usuario: ${ex.message}"
                        }
                }
            }
        )
    }

    // Modal Editar Usuario
    usuarioEditar?.let { u ->
        ModalEditarUsuarioAdmin(
            usuario = u,
            onDesestimar = { usuarioEditar = null },
            onGuardar = { req ->
                scope.launch {
                    adminService.updateUser(u.id, req)
                        .onSuccess { act ->
                            usuarioEditar = null
                            mensaje = "Usuario ${act.nombre} actualizado correctamente."
                            cargarUsuarios()
                        }
                        .onFailure { ex ->
                            error = "Error al actualizar usuario: ${ex.message}"
                        }
                }
            }
        )
    }
}

@Composable
private fun ItemUsuarioAdmin(
    usuarioItem: UserDto,
    esAdminActual: Boolean,
    onEditar: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FocusUpColors.Tarjeta),
        border = BordeTarjeta
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FocusUpColors.TarjetaElevada),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuarioItem.nombre.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = FocusUpColors.AzulElectrico,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = usuarioItem.nombre,
                        fontWeight = FontWeight.Bold,
                        color = FocusUpColors.Texto,
                        fontSize = 15.sp
                    )
                    if (esAdminActual) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(Tú)", fontSize = 11.sp, color = FocusUpColors.AzulCyan, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = usuarioItem.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusUpColors.TextoSecundario
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FocusUpColors.AzulElectrico.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = usuarioItem.rol.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = FocusUpColors.AzulElectrico,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (usuarioItem.activo) FocusUpColors.VerdeExito.copy(alpha = 0.15f) else FocusUpColors.Error.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (usuarioItem.activo) "ACTIVO" else "INACTIVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (usuarioItem.activo) FocusUpColors.VerdeExito else FocusUpColors.Error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onEditar) {
                Text("✏️", fontSize = 16.sp)
            }

            // Switch para activar/desactivar estado (Deshabilitado si es el admin actual)
            Switch(
                checked = usuarioItem.activo,
                onCheckedChange = { nuevoEstado ->
                    if (!esAdminActual) {
                        onToggleStatus(nuevoEstado)
                    }
                },
                enabled = !esAdminActual,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FocusUpColors.Fondo,
                    checkedTrackColor = FocusUpColors.VerdeExito,
                    uncheckedThumbColor = FocusUpColors.TextoSecundario,
                    uncheckedTrackColor = FocusUpColors.TarjetaElevada
                )
            )
        }
    }
}

@Composable
private fun ModalCrearUsuarioAdmin(
    onDesestimar: () -> Unit,
    onGuardar: (CreateUserRequest) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.ESTUDIANTE) }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("➕ Registrar Nuevo Usuario", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorValidacion = null },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; errorValidacion = null },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorValidacion = null },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rol del usuario:", style = MaterialTheme.typography.labelMedium, color = FocusUpColors.TextoSecundario)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Rol.ESTUDIANTE, Rol.DOCENTE, Rol.ADMINISTRADOR).forEach { r ->
                        FilterChip(
                            selected = rolSeleccionado == r,
                            onClick = { rolSeleccionado = r },
                            label = { Text(r.name, fontSize = 11.sp) }
                        )
                    }
                }

                errorValidacion?.let { err ->
                    Text(err, color = FocusUpColors.Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val correoTrim = correo.trim()
                    val esEmailValido = correoTrim.contains("@") && correoTrim.contains(".")
                    when {
                        nombre.isBlank() -> errorValidacion = "El nombre es obligatorio."
                        correoTrim.isBlank() -> errorValidacion = "El correo es obligatorio."
                        !esEmailValido -> errorValidacion = "Formato de correo inválido (ej. usuario@focusup.com)."
                        password.isBlank() -> errorValidacion = "La contraseña es obligatoria."
                        else -> {
                            onGuardar(
                                CreateUserRequest(
                                    nombre = nombre.trim(),
                                    correo = correoTrim,
                                    password = password,
                                    rol = rolSeleccionado
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Registrar", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
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
private fun ModalEditarUsuarioAdmin(
    usuario: UserDto,
    onDesestimar: () -> Unit,
    onGuardar: (UpdateUserRequest) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var correo by remember { mutableStateOf(usuario.correo) }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDesestimar,
        containerColor = FocusUpColors.TarjetaElevada,
        title = {
            Text("✏️ Editar Usuario #${usuario.id}", color = FocusUpColors.Texto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorValidacion = null },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; errorValidacion = null },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nueva Contraseña (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rol del usuario:", style = MaterialTheme.typography.labelMedium, color = FocusUpColors.TextoSecundario)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Rol.ESTUDIANTE, Rol.DOCENTE, Rol.ADMINISTRADOR).forEach { r ->
                        FilterChip(
                            selected = rolSeleccionado == r,
                            onClick = { rolSeleccionado = r },
                            label = { Text(r.name, fontSize = 11.sp) }
                        )
                    }
                }

                errorValidacion?.let { err ->
                    Text(err, color = FocusUpColors.Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val correoTrim = correo.trim()
                    val esEmailValido = correoTrim.contains("@") && correoTrim.contains(".")
                    when {
                        nombre.isBlank() -> errorValidacion = "El nombre no puede estar vacío."
                        correoTrim.isBlank() -> errorValidacion = "El correo no puede estar vacío."
                        !esEmailValido -> errorValidacion = "Formato de correo inválido."
                        else -> {
                            onGuardar(
                                UpdateUserRequest(
                                    nombre = nombre.trim(),
                                    correo = correoTrim,
                                    rol = rolSeleccionado,
                                    password = password.ifBlank { null }
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusUpColors.AzulElectrico)
            ) {
                Text("Guardar Cambios", color = FocusUpColors.Fondo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDesestimar) {
                Text("Cancelar", color = FocusUpColors.TextoSecundario)
            }
        }
    )
}
