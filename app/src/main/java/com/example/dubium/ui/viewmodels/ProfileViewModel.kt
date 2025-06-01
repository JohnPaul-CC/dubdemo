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
        println("🔄 ProfileViewModel: Iniciando loadUserProfile()") // Debug
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                println("🔑 ProfileViewModel: Token obtenido: ${token?.take(50)}...") // Debug

                if (token != null) {
                    println("📡 ProfileViewModel: Llamando a getUserProfile...") // Debug
                    userRepository.getUserProfile(token).fold(
                        onSuccess = { userInfo ->
                            println("✅ ProfileViewModel: Respuesta exitosa: $userInfo")

                            // ✅ CAMBIAR ESTA LÍNEA - ahora userInfo ya tiene la estructura correcta
                            val userData = userInfo["data"] as? Map<String, Any>
                            println("👤 ProfileViewModel: UserData extraída: $userData")

                            if (userData != null) {
                                val userDto = UserDto(
                                    id = (userData["id"] as? Double)?.toInt() ?: 0,
                                    username = userData["username"] as? String ?: "Usuario",
                                    createdAt = userData["createdAt"] as? String ?: ""
                                )
                                println("✅ ProfileViewModel: UserDto creado: $userDto")

                                _user.value = userDto
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isUserLoaded = true
                                )
                            } else {
                                println("❌ ProfileViewModel: userData es null")
                                handleProfileError("Error obteniendo datos del usuario")
                            }
                        },
                        onFailure = { exception ->
                            println("❌ ProfileViewModel: Error en API call: ${exception.message}")
                            handleProfileError(exception.message ?: "Error de conexión")
                        }
                    )
                } else {
                    println("❌ ProfileViewModel: Token es null") // Debug
                    handleProfileError("Sesión expirada")
                }
            } catch (e: Exception) {
                println("💥 ProfileViewModel: Excepción: ${e.message}") // Debug
                e.printStackTrace()
                handleProfileError("Error inesperado: ${e.message}")
            }
        }
    }

    fun resetState() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _user.value = null
            _uiState.value = ProfileUiState()
        }
    }

    /**
     * Realizar logout
     */
    fun logout(onLogoutComplete: () -> Unit) {
        println("🚪 ProfileViewModel: logout() iniciado") // Debug
        _uiState.value = _uiState.value.copy(isLoggingOut = true)

        viewModelScope.launch {
            try {
                println("🚪 ProfileViewModel: Intentando logout en servidor...") // Debug
                val token = tokenManager.getToken()
                if (token != null) {
                    userRepository.logout(token)
                    println("🚪 ProfileViewModel: Logout en servidor completado") // Debug
                }
            } catch (e: Exception) {
                println("🚪 ProfileViewModel: Error en logout servidor: ${e.message}") // Debug
            } finally {
                println("🚪 ProfileViewModel: Limpiando token local...") // Debug
                tokenManager.clearToken()

                // Reset estado
                _user.value = null
                _uiState.value = ProfileUiState()

                println("🚪 ProfileViewModel: Llamando onLogoutComplete()") // Debug
                onLogoutComplete()
            }
        }
    }

    /**
     * Refrescar datos del usuario
     */
    fun refreshProfile() {
        println("🔄 ProfileViewModel: refreshProfile() llamado") // Debug
        println("🔄 ProfileViewModel: Estado actual: ${_uiState.value}") // Debug
        loadUserProfile()
    }

    /**
     * Verificar si el token sigue siendo válido
     */
    fun verifyTokenValidity(onTokenInvalid: () -> Unit) {
        println("🔍 ProfileViewModel: verifyTokenValidity() iniciado")
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                println("🔍 ProfileViewModel: Token para verificar: ${token?.take(50)}...")

                if (token != null) {
                    userRepository.verifyToken(token).fold(
                        onSuccess = {
                            println("✅ ProfileViewModel: Token válido")
                            // Token válido - no hacer nada
                        },
                        onFailure = { error ->
                            println("❌ ProfileViewModel: Token inválido: ${error.message}")

                            // ✅ Solo limpiar token si es realmente inválido (401)
                            // No limpiar por errores de red (timeout, etc.)
                            if (error.message?.contains("Token inválido") == true ||
                                error.message?.contains("no válido") == true) {
                                println("🗑️ ProfileViewModel: Limpiando token inválido...")
                                tokenManager.clearToken()
                                onTokenInvalid()
                            } else {
                                println("⚠️ ProfileViewModel: Error de red, manteniendo token")
                                // No limpiar token por errores temporales
                            }
                        }
                    )
                } else {
                    println("❌ ProfileViewModel: No hay token")
                    onTokenInvalid()
                }
            } catch (e: Exception) {
                println("💥 ProfileViewModel: Excepción en verifyTokenValidity: ${e.message}")
                // ✅ No limpiar token por excepciones de red
                println("⚠️ ProfileViewModel: Manteniendo token por excepción")
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