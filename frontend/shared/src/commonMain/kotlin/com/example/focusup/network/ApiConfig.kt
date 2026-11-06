package com.example.focusup.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Configuración global de red y cliente Ktor.
 */
object ApiConfig {
    const val BASE_URL = "http://192.168.1.67:8080"
    // const val BASE_URL = "http://10.0.2.2:8080"

    /** Token JWT retenido durante la sesión en memoria */
    var tokenSesion: String? = null

    /** Cliente Ktor compartido con serialización JSON tolerante */
    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
        }
    }
}
