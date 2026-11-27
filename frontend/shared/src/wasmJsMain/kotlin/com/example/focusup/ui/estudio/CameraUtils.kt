package com.example.focusup.ui.estudio

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberCameraLauncher(
    onPictureTaken: (ByteArray) -> Unit
): () -> Unit {
    return {
        // Fallback temporal para Web (wasmJs)
    }
}

actual fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return null
}
