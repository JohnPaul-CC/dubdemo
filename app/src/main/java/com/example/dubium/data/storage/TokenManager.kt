package com.example.dubium.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Gestión de tokens JWT usando DataStore
 * Persistente, seguro y compatible con coroutines
 */
class TokenManager(private val context: Context) {

    // 🗃️ Claves para DataStore
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val LOGIN_TIMESTAMP_KEY = stringPreferencesKey("login_timestamp")

        // Extension para crear DataStore
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
    }

    // 📄 DataStore instance
    private val dataStore = context.dataStore

    // 💾 OPERACIONES DE TOKEN

    /**
     * Guardar token JWT después de login exitoso
     * @param token - JWT token completo
     * @param username - nombre del usuario logueado
     * @param userId - ID del usuario
     */
    suspend fun saveToken(token: String, username: String? = null, userId: Int? = null) {
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[LOGIN_TIMESTAMP_KEY] = System.currentTimeMillis().toString()

            username?.let { preferences[USERNAME_KEY] = it }
            userId?.let { preferences[USER_ID_KEY] = it.toString() }
        }
    }

    /**
     * Obtener token JWT guardado
     * @return String? - token o null si no existe
     */
    suspend fun getToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[JWT_TOKEN_KEY]
    }

    /**
     * Verificar si hay token guardado
     * @return Boolean - true si existe token
     */
    suspend fun hasToken(): Boolean {
        return getToken() != null
    }

    /**
     * Obtener token como Flow para observar cambios
     * @return Flow<String?> - flujo del token
     */
    fun getTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[JWT_TOKEN_KEY]
        }
    }

    /**
     * Limpiar token y datos relacionados (logout)
     */
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(LOGIN_TIMESTAMP_KEY)
        }
    }

    // 👤 INFORMACIÓN DEL USUARIO

    /**
     * Obtener username guardado
     * @return String? - username o null
     */
    suspend fun getUsername(): String? {
        val preferences = dataStore.data.first()
        return preferences[USERNAME_KEY]
    }

    /**
     * Obtener ID del usuario guardado
     * @return Int? - user ID o null
     */
    suspend fun getUserId(): Int? {
        val preferences = dataStore.data.first()
        return preferences[USER_ID_KEY]?.toIntOrNull()
    }

    /**
     * Obtener timestamp del login
     * @return Long? - timestamp en milisegundos o null
     */
    suspend fun getLoginTimestamp(): Long? {
        val preferences = dataStore.data.first()
        return preferences[LOGIN_TIMESTAMP_KEY]?.toLongOrNull()
    }

    /**
     * Obtener información completa del usuario como Flow
     * @return Flow<UserInfo?> - flujo con info del usuario
     */
    fun getUserInfoFlow(): Flow<UserInfo?> {
        return dataStore.data.map { preferences ->
            val token = preferences[JWT_TOKEN_KEY]
            val username = preferences[USERNAME_KEY]
            val userId = preferences[USER_ID_KEY]?.toIntOrNull()
            val timestamp = preferences[LOGIN_TIMESTAMP_KEY]?.toLongOrNull()

            if (token != null) {
                UserInfo(
                    token = token,
                    username = username,
                    userId = userId,
                    loginTimestamp = timestamp
                )
            } else {
                null
            }
        }
    }

    // 🔍 UTILIDADES

    /**
     * Verificar si el token está próximo a expirar
     * Los tokens duran 30 días, alertar si quedan menos de 3 días
     * @return Boolean - true si expira pronto
     */
    suspend fun isTokenExpiringSoon(): Boolean {
        val loginTimestamp = getLoginTimestamp() ?: return false
        val now = System.currentTimeMillis()
        val daysSinceLogin = (now - loginTimestamp) / (24 * 60 * 60 * 1000)

        return daysSinceLogin >= 27  // Alertar si faltan 3 días o menos
    }

    /**
     * Obtener días restantes del token
     * @return Int - días hasta expiración (puede ser negativo si ya expiró)
     */
    suspend fun getDaysUntilExpiry(): Int {
        val loginTimestamp = getLoginTimestamp() ?: return 0
        val now = System.currentTimeMillis()
        val daysSinceLogin = (now - loginTimestamp) / (24 * 60 * 60 * 1000)

        return (30 - daysSinceLogin).toInt()  // 30 días de duración total
    }

    /**
     * Crear header de autorización completo
     * @return String? - "Bearer token" o null si no hay token
     */
    suspend fun getAuthorizationHeader(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }

    /**
     * Verificar estado de autenticación
     * @return AuthStatus - estado actual de la sesión
     */
    suspend fun getAuthStatus(): AuthStatus {
        val token = getToken()
        return when {
            token == null -> AuthStatus.NOT_LOGGED_IN
            isTokenExpiringSoon() -> AuthStatus.EXPIRING_SOON
            else -> AuthStatus.LOGGED_IN
        }
    }

    /**
     * Limpiar datos expirados (llamar periódicamente)
     */
    suspend fun cleanupExpiredData() {
        val daysRemaining = getDaysUntilExpiry()
        if (daysRemaining <= 0) {
            clearToken()  // Auto-logout si token expirado
        }
    }
}

/**
 * Data class con información completa del usuario
 */
data class UserInfo(
    val token: String,
    val username: String?,
    val userId: Int?,
    val loginTimestamp: Long?
)

/**
 * Estados de autenticación posibles
 */
enum class AuthStatus {
    NOT_LOGGED_IN,      // No hay token
    LOGGED_IN,          // Token válido
    EXPIRING_SOON       // Token expira pronto
}