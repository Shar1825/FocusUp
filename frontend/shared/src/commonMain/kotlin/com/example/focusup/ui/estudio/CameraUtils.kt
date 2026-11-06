package com.example.focusup.ui.estudio

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberCameraLauncher(
    onPictureTaken: (ByteArray) -> Unit
): () -> Unit

expect fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap?
