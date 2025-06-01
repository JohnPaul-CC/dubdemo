package com.example.dubium.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.dubium.data.repository.UserRepository
import com.example.dubium.data.storage.TokenManager

/**
 * ViewModel para manejar la lógica de login
 * Maneja el estado de la UI y coordina con Repository
 */
class LoginViewModel(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 🎯 Estados de UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 📝 Campos del formulario
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    /**
     * Actualizar username
     */
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        clearError() // Limpiar error cuando usuario escribe
    }

    /**
     * Actualizar password
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        clearError() // Limpiar error cuando usuario escribe
    }

    /**
     * Realizar login con credenciales actuales
     */
    fun login(onSuccess: (String) -> Unit) {
        val currentUsername = _username.value.trim()
        val currentPassword = _password.value

        // Validaciones básicas
        if (currentUsername.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Username requerido"
            )
            return
        }

        if (currentPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Password requerido"
            )
            return
        }

        // Iniciar loading
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = ""
        )

        viewModelScope.launch {
            try {
                userRepository.login(currentUsername, currentPassword).fold(
                    onSuccess = { authResponse ->
                        if (authResponse.success && authResponse.token != null) {
                            // Guardar token
                            tokenManager.saveToken(authResponse.token)

                            // Actualizar estado de éxito
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                                errorMessage = ""
                            )

                            // Callback de éxito con username
                            val displayName = authResponse.user?.username ?: currentUsername
                            onSuccess(displayName)

                        } else {
                            // Error en respuesta
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = authResponse.message ?: "Error de login"
                            )
                        }
                    },
                    onFailure = { exception ->
                        // Error de red o repository
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error de conexión"
                        )
                    }
                )
            } catch (e: Exception) {
                // Error inesperado
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    /**
     * Reset del estado (para cuando se navega de vuelta)
     */
    fun resetState() {
        _uiState.value = LoginUiState()
        _username.value = ""
        _password.value = ""
    }

    /**
     * Verificar si hay token guardado para auto-login
     */
    fun checkAutoLogin(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val savedToken = tokenManager.getToken()
                if (savedToken != null) {
                    // Verificar token con API
                    userRepository.verifyToken(savedToken).fold(
                        onSuccess = { userInfo ->
                            // Token válido - auto login exitoso
                            onSuccess(userInfo["username"] as? String ?: "Usuario")
                        },
                        onFailure = {
                            // Token inválido - limpiar
                            tokenManager.clearToken()
                        }
                    )
                }
            } catch (e: Exception) {
                // Error verificando - continuar normalmente
                tokenManager.clearToken()
            }
        }
    }
}

/**
 * Estado de UI para LoginScreen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String = ""
) {
    // Computed properties para facilitar uso en UI
    val showError: Boolean get() = errorMessage.isNotEmpty()
    val enableLoginButton: Boolean get() = !isLoading
}