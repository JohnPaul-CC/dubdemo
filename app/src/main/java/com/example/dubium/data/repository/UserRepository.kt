package com.example.dubium.data.repository

import com.example.dubium.data.dto.*
import com.example.dubium.data.api.ApiService
import com.example.dubium.data.api.ApiClient
import retrofit2.Response

/**
 * Repository que maneja todas las operaciones relacionadas con usuarios
 * Actúa como capa intermedia entre ViewModels y ApiService
 */
class UserRepository(
    private val apiService: ApiService = ApiClient.apiService
) {

    // 🔐 AUTENTICACIÓN

    /**
     * Realizar login con username y password
     * @param username - nombre de usuario
     * @param password - contraseña
     * @return Result<AuthResponse> - resultado del login
     */
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response) ?: "Error de login"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Registrar nuevo usuario
     * @param username - nombre de usuario deseado
     * @param password - contraseña
     * @return Result<AuthResponse> - resultado del registro
     */
    suspend fun register(username: String, password: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(username, password)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response) ?: "Error de registro"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Verificar si token JWT es válido
     * @param token - JWT token
     * @return Result<Map<String, Any>> - resultado de verificación
     */
    suspend fun verifyToken(token: String): Result<Map<String, Any>> {
        return try {
            println("🔍 UserRepository: verifyToken llamado") // Debug
            val authHeader = ApiClient.createAuthHeader(token)
            val response = apiService.verifyToken(authHeader)

            println("🔍 UserRepository: verifyToken response code: ${response.code()}") // Debug

            if (response.isSuccessful && response.body() != null) {
                println("✅ UserRepository: Token verificado exitosamente") // Debug
                Result.success(response.body()!!)
            } else {
                println("❌ UserRepository: Token verification failed") // Debug
                Result.failure(Exception("Token inválido"))
            }
        } catch (e: Exception) {
            println("💥 UserRepository: Excepción en verifyToken: ${e.message}") // Debug
            Result.failure(Exception("Error verificando token: ${e.message}"))
        }
    }

    // 👤 PERFIL DE USUARIO

    /**
     * Obtener perfil del usuario actual
     * @param token - JWT token
     * @return Result<Map<String, Any>> - datos del perfil
     */
    suspend fun getUserProfile(token: String): Result<Map<String, Any>> {
        return try {
            println("🔑 UserRepository: Token recibido: ${token.take(50)}...") // Debug
            val authHeader = ApiClient.createAuthHeader(token)
            println("📋 UserRepository: Auth header: ${authHeader.take(60)}...") // Debug

            val response = apiService.getUserProfile(authHeader)
            println("📡 UserRepository: Response code: ${response.code()}") // Debug
            println("📡 UserRepository: Response successful: ${response.isSuccessful}") // Debug

            if (response.isSuccessful && response.body() != null) {
                println("✅ UserRepository: Response body: ${response.body()}") // Debug
                Result.success(response.body()!!)
            } else {
                val errorMsg = if (ApiClient.isTokenExpired(response.code())) {
                    "Sesión expirada"
                } else {
                    "Error obteniendo perfil"
                }
                println("❌ UserRepository: Error - $errorMsg") // Debug
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            println("💥 UserRepository: Excepción: ${e.message}") // Debug
            e.printStackTrace()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Realizar logout
     * @param token - JWT token
     * @return Result<Unit> - resultado del logout
     */
    suspend fun logout(token: String): Result<Unit> {
        return try {
            val authHeader = ApiClient.createAuthHeader(token)
            val response = apiService.logout(authHeader)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Logout local aunque falle en servidor
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Logout local aunque haya error de red
            Result.success(Unit)
        }
    }

    // 🔧 UTILIDADES

    /**
     * Probar conectividad con la API
     * @return Boolean - true si la API responde
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
     * Health check de la API
     * @return Result<String> - estado de la API
     */
    suspend fun healthCheck(): Result<String> {
        return try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                Result.success(response.body() ?: "API funcionando")
            } else {
                Result.failure(Exception("API no disponible"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener usuarios para debugging (solo desarrollo)
     * @return Result<Map<String, Any>> - lista de usuarios
     */
    suspend fun getDebugUsers(): Result<Map<String, Any>> {
        return try {
            val response = apiService.getDebugUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error obteniendo usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // 🔮 FUNCIONES FUTURAS (para preguntas y respuestas)

    /**
     * Crear nueva pregunta (futuro)
     */
    suspend fun createQuestion(token: String, title: String, content: String): Result<Map<String, Any>> {
        return try {
            val authHeader = ApiClient.createAuthHeader(token)
            val request = mapOf("title" to title, "content" to content)
            val response = apiService.createQuestion(authHeader, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error creando pregunta"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener lista de preguntas (futuro)
     */
    suspend fun getQuestions(token: String): Result<Map<String, Any>> {
        return try {
            val authHeader = ApiClient.createAuthHeader(token)
            val response = apiService.getQuestions(authHeader)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error obteniendo preguntas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // 🛠️ FUNCIONES PRIVADAS

    /**
     * Parsear mensaje de error desde respuesta HTTP
     */
    private fun parseErrorMessage(response: Response<*>): String? {
        return try {
            // Intentar parsear error del cuerpo de respuesta
            val errorBody = response.errorBody()?.string()
            // Aquí podrías parsear JSON de error si el servidor lo envía
            errorBody ?: when (response.code()) {
                400 -> "Datos inválidos"
                401 -> "Credenciales incorrectas"
                404 -> "Usuario no encontrado"
                500 -> "Error interno del servidor"
                else -> "Error desconocido"
            }
        } catch (e: Exception) {
            null
        }
    }
}