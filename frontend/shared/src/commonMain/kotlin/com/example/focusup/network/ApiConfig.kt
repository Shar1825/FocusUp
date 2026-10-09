package com.example.focusup.network

/**
 * Configuración de red en un solo lugar.
 *
 * Cambia BASE_URL según dónde pruebes la app:
 *  - Celular físico en la misma red Wi-Fi: IP de tu computadora (ej. http://192.168.1.67:8080)
 *  - Emulador Android: http://10.0.2.2:8080
 *  - Escritorio (Desktop) o Web: http://localhost:8080
 */
object ApiConfig {
    const val BASE_URL = "http://192.168.1.67:8080"
    //const val BASE_URL = "http://10.0.2.2:8080"
}