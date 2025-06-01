package com.example.dubium.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.dubium.data.repository.UserRepository
import com.example.dubium.data.storage.TokenManager
import com.example.dubium.data.dto.UserDto

/**
 * ViewModel para la pantalla principal/perfil del usuario
 * Maneja información del usuario logueado y logout
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 🎯 Estados de UI
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // 👤 Información del usuario
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    init {
        // Cargar información del usuario al crear el ViewModel
        loadUserProfile()
    }

    /**
     * Cargar perfil del usuario desde la API
     */
    fun loadUserProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    userRepository.getUserProfile(token).fold(
                        onSuccess = { userInfo ->
                            // Extraer información del usuario del Map
                            val userData = userInfo["data"] as? Map<String, Any>
                            if (userData != null) {
                                val userDto = UserDto(
                                    id = (userData["id"] as? Double)?.toInt() ?: 0,
                                    username = userData["username"] as? String ?: "Usuario",
                                    createdAt = userData["createdAt"] as? String ?: ""
                                )

                                _user.value = userDto
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isUserLoaded = true
                                )
                            } else {
                                handleProfileError("Error obteniendo datos del usuario")
                            }
                        },
                        onFailure = { exception ->
                            handleProfileError(exception.message ?: "Error de conexión")
                        }
                    )
                } else {
                    // No hay token - shouldn't happen pero manejar
                    handleProfileError("Sesión expirada")
                }
            } catch (e: Exception) {
                handleProfileError("Error inesperado: ${e.message}")
            }
        }
    }

    /**
     * Realizar logout
     */
    fun logout(onLogoutComplete: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoggingOut = true)

        viewModelScope.launch {
            try {
                // Opcional: notificar al servidor sobre logout
                val token = tokenManager.getToken()
                if (token != null) {
                    // Intentar logout en servidor (no crítico si falla)
                    userRepository.logout(token)
                }
            } catch (e: Exception) {
                // Ignorar errores de logout en servidor
            } finally {
                // Siempre limpiar token local
                tokenManager.clearToken()

                // Reset estado
                _user.value = null
                _uiState.value = ProfileUiState()

                // Notificar que logout completó
                onLogoutComplete()
            }
        }
    }

    /**
     * Refrescar datos del usuario
     */
    fun refreshProfile() {
        loadUserProfile()
    }

    /**
     * Verificar si el token sigue siendo válido
     */
    fun verifyTokenValidity(onTokenInvalid: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    userRepository.verifyToken(token).fold(
                        onSuccess = {
                            // Token válido - no hacer nada
                        },
                        onFailure = {
                            // Token inválido - limpiar y redirigir
                            tokenManager.clearToken()
                            onTokenInvalid()
                        }
                    )
                } else {
                    // No hay token
                    onTokenInvalid()
                }
            } catch (e: Exception) {
                // En caso de error, asumir token inválido
                tokenManager.clearToken()
                onTokenInvalid()
            }
        }
    }

    /**
     * Obtener saludo personalizado
     */
    fun getGreeting(): String {
        val currentUser = _user.value
        return if (currentUser != null) {
            "Hola ${currentUser.username}"
        } else {
            "Hola Usuario"
        }
    }

    /**
     * Obtener información de la cuenta
     */
    fun getAccountInfo(): Map<String, String> {
        val currentUser = _user.value
        return if (currentUser != null) {
            mapOf(
                "ID" to currentUser.id.toString(),
                "Username" to currentUser.username,
                "Miembro desde" to formatDate(currentUser.createdAt)
            )
        } else {
            emptyMap()
        }
    }

    /**
     * Manejar errores al cargar perfil
     */
    private fun handleProfileError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    /**
     * Formatear fecha para mostrar
     */
    private fun formatDate(dateString: String): String {
        return try {
            // Simplificado - en una app real usarías DateTimeFormatter
            dateString.substringBefore("T")
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}

/**
 * Estado de UI para HomeScreen/ProfileScreen
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isUserLoaded: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String = ""
) {
    // Computed properties para facilitar uso en UI
    val showError: Boolean get() = errorMessage.isNotEmpty()
    val showContent: Boolean get() = isUserLoaded && !isLoading
    val enableLogout: Boolean get() = !isLoggingOut && !isLoading
}