package com.example.dubium.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de API que configura Retrofit para comunicarse con el backend
 * Singleton que se reutiliza en toda la app
 */
object ApiClient {

    // 🌐 URLs de conexión
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/"     // Para emulador Android Studio
    private const val BASE_URL_DEVICE = "http://192.168.0.23:8080/"  // Para dispositivo físico (cambiar IP)
    private const val BASE_URL_LOCALHOST = "http://localhost:8080/"    // Para testing en PC

    // Usar URL según el entorno
    private const val BASE_URL = BASE_URL_DEVICE // ← Cambiar según necesites

    // 📝 Configuración de logging para debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Ver requests y responses completos
        // En producción usar: HttpLoggingInterceptor.Level.NONE
    }

    // 🔧 Cliente HTTP con configuraciones
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)           // Logging de requests
        .connectTimeout(30, TimeUnit.SECONDS)        // Timeout para conectar
        .readTimeout(30, TimeUnit.SECONDS)           // Timeout para leer respuesta
        .writeTimeout(30, TimeUnit.SECONDS)          // Timeout para enviar datos
        .retryOnConnectionFailure(true)              // Reintentar en caso de error de red
        .build()

    // 🏭 Instancia de Retrofit configurada
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)                           // URL base de tu API
        .client(httpClient)                          // Cliente HTTP configurado
        .addConverterFactory(GsonConverterFactory.create())  // Convertir JSON ↔ objetos Kotlin
        .build()

    // 📡 Instancia del servicio de API (singleton)
    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // 🛠️ Funciones de utilidad

    /**
     * Crear header de autorización con formato correcto
     * @param token - JWT token sin el prefijo "Bearer"
     * @return String - header completo "Bearer eyJ0eXAi..."
     */
    fun createAuthHeader(token: String): String {
        return "Bearer $token"
    }

    /**
     * Verificar si una respuesta indica token expirado
     * @param responseCode - código HTTP de respuesta
     * @return Boolean - true si token está expirado
     */
    fun isTokenExpired(responseCode: Int): Boolean {
        return responseCode == 401  // Unauthorized
    }

    /**
     * Obtener URL base actual (para debugging)
     */
    fun getBaseUrl(): String = BASE_URL

    /**
     * Verificar conectividad con la API
     * Función suspendida para usar en coroutines
     */
    suspend fun testConnection(): Boolean {
        return try {
            val response = apiService.testConnection()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Configuración para diferentes entornos
     */
    object Config {
        const val EMULATOR_IP = "10.0.2.2"          // IP especial del emulador
        const val LOCALHOST = "localhost"           // Para testing local

        /**
         * Crear URL para dispositivo físico
         * @param localIp - IP de tu computadora en la red local
         */
        fun createDeviceUrl(localIp: String): String {
            return "http://$localIp:8080/"
        }

        /**
         * Detectar automáticamente qué URL usar
         * TODO: Implementar detección automática de emulador vs dispositivo
         */
        fun getOptimalBaseUrl(): String {
            // Por ahora retorna la configurada
            return BASE_URL
        }
    }

    /**
     * Configuraciones de timeout personalizadas
     */
    object Timeouts {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L

        /**
         * Crear cliente con timeouts personalizados
         */
        fun createCustomClient(
            connectTimeout: Long = CONNECT_TIMEOUT,
            readTimeout: Long = READ_TIMEOUT,
            writeTimeout: Long = WRITE_TIMEOUT
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }
    }
}