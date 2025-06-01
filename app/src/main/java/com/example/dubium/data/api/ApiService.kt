package com.example.dubium.data.api

import retrofit2.Response
import retrofit2.http.*
import com.example.dubium.data.dto.*

/**
 * Interface que define todos los endpoints de la API
 * Retrofit convierte estas funciones en llamadas HTTP automáticamente
 */
interface ApiService {

    // 🔓 ENDPOINTS PÚBLICOS (no requieren autenticación)

    /**
     * Endpoint de prueba para verificar conectividad
     * GET /test
     */
    @GET("test")
    suspend fun testConnection(): Response<Map<String, Any>>

    /**
     * Health check de la API
     * GET /
     */
    @GET("/")
    suspend fun healthCheck(): Response<String>

    /**
     * Registrar nuevo usuario
     * POST /auth/register
     * Body: {"username":"juan","password":"123456"}
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * Login de usuario existente
     * POST /auth/login
     * Body: {"username":"juan","password":"123456"}
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // 🔒 ENDPOINTS PROTEGIDOS (requieren JWT token)

    /**
     * Verificar si token JWT es válido
     * GET /auth/verify
     * Header: Authorization: Bearer eyJ0eXAi...
     */
    @GET("auth/verify")
    suspend fun verifyToken(
        @Header("Authorization") authorization: String
    ): Response<Map<String, Any>>

    /**
     * Obtener perfil del usuario actual
     * GET /user/profile
     * Header: Authorization: Bearer eyJ0eXAi...
     */
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String
    ): Response<Map<String, Any>>

    /**
     * Logout del usuario
     * POST /auth/logout
     * Header: Authorization: Bearer eyJ0eXAi...
     */
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): Response<Map<String, Any>>

    // 🧪 ENDPOINTS DE DESARROLLO

    /**
     * Obtener lista de usuarios (solo desarrollo)
     * GET /debug/users
     */
    @GET("debug/users")
    suspend fun getDebugUsers(): Response<Map<String, Any>>

    // 🔮 ENDPOINTS FUTUROS (para cuando agregues más funcionalidades)

    /**
     * Crear nueva pregunta (futuro)
     * POST /questions
     * Header: Authorization: Bearer eyJ0eXAi...
     * Body: {"title":"¿Cómo usar Kotlin?","content":"Explicación..."}
     */
    @POST("questions")
    suspend fun createQuestion(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, String>  // Temporal - crear QuestionRequest después
    ): Response<Map<String, Any>>

    /**
     * Obtener lista de preguntas (futuro)
     * GET /questions
     * Header: Authorization: Bearer eyJ0eXAi...
     */
    @GET("questions")
    suspend fun getQuestions(
        @Header("Authorization") authorization: String
    ): Response<Map<String, Any>>

    /**
     * Obtener pregunta específica con respuestas (futuro)
     * GET /questions/{id}
     * Header: Authorization: Bearer eyJ0eXAi...
     */
    @GET("questions/{id}")
    suspend fun getQuestion(
        @Header("Authorization") authorization: String,
        @Path("id") questionId: Int
    ): Response<Map<String, Any>>

    /**
     * Crear respuesta a pregunta (futuro)
     * POST /answers
     * Header: Authorization: Bearer eyJ0eXAi...
     * Body: {"content":"Respuesta...","questionId":123}
     */
    @POST("answers")
    suspend fun createAnswer(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, Any>  // Temporal - crear AnswerRequest después
    ): Response<Map<String, Any>>

    /**
     * Votar pregunta o respuesta (futuro)
     * POST /votes
     * Header: Authorization: Bearer eyJ0eXAi...
     * Body: {"value":1,"questionId":123} o {"value":-1,"answerId":456}
     */
    @POST("votes")
    suspend fun vote(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, Any>  // Temporal - crear VoteRequest después
    ): Response<Map<String, Any>>
}