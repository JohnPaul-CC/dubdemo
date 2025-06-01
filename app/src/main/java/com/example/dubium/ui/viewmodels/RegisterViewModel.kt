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
 * ViewModel para manejar la lógica de registro
 * Maneja validaciones, estado de UI y coordina con Repository
 */
class RegisterViewModel(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 🎯 Estados de UI
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // 📝 Campos del formulario
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    /**
     * Actualizar username con validación en tiempo real
     */
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        validateUsername(newUsername)
        clearGeneralError()
    }

    /**
     * Actualizar password con validación en tiempo real
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
        clearGeneralError()

        // Re-validar confirmación si ya se ingresó
        if (_confirmPassword.value.isNotEmpty()) {
            validatePasswordConfirmation(_confirmPassword.value, newPassword)
        }
    }

    /**
     * Actualizar confirmación de password
     */
    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        validatePasswordConfirmation(newConfirmPassword, _password.value)
        clearGeneralError()
    }

    /**
     * Realizar registro con datos actuales
     */
    fun register(onSuccess: (String) -> Unit) {
        val currentUsername = _username.value.trim()
        val currentPassword = _password.value
        val currentConfirmPassword = _confirmPassword.value

        // Validar todo antes de enviar
        if (!validateAllFields(currentUsername, currentPassword, currentConfirmPassword)) {
            return
        }

        // Iniciar loading
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            generalError = ""
        )

        viewModelScope.launch {
            try {
                userRepository.register(currentUsername, currentPassword).fold(
                    onSuccess = { authResponse ->
                        if (authResponse.success && authResponse.token != null) {
                            // Guardar token
                            tokenManager.saveToken(authResponse.token)

                            // Actualizar estado de éxito
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isRegistrationSuccessful = true,
                                generalError = ""
                            )

                            // Callback de éxito
                            val displayName = authResponse.user?.username ?: currentUsername
                            onSuccess(displayName)

                        } else {
                            // Error en respuesta
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                generalError = authResponse.message ?: "Error de registro"
                            )
                        }
                    },
                    onFailure = { exception ->
                        // Error de red o repository
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = exception.message ?: "Error de conexión"
                        )
                    }
                )
            } catch (e: Exception) {
                // Error inesperado
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Validar username
     */
    private fun validateUsername(username: String) {
        val error = when {
            username.isEmpty() -> ""  // No mostrar error mientras escribe
            username.length < 3 -> "Username debe tener al menos 3 caracteres"
            username.length > 50 -> "Username no puede tener más de 50 caracteres"
            !username.matches("^[a-zA-Z0-9_]+$".toRegex()) ->
                "Username solo puede contener letras, números y guión bajo"
            else -> ""
        }

        _uiState.value = _uiState.value.copy(usernameError = error)
    }

    /**
     * Validar password
     */
    private fun validatePassword(password: String) {
        val error = when {
            password.isEmpty() -> ""  // No mostrar error mientras escribe
            password.length < 4 -> "Password debe tener al menos 4 caracteres"
            password.length > 100 -> "Password no puede tener más de 100 caracteres"
            password.contains(" ") -> "Password no puede contener espacios"
            else -> ""
        }

        _uiState.value = _uiState.value.copy(passwordError = error)
    }

    /**
     * Validar confirmación de password
     */
    private fun validatePasswordConfirmation(confirmPassword: String, originalPassword: String) {
        val error = when {
            confirmPassword.isEmpty() -> ""  // No mostrar error mientras escribe
            confirmPassword != originalPassword -> "Las contraseñas no coinciden"
            else -> ""
        }

        _uiState.value = _uiState.value.copy(confirmPasswordError = error)
    }

    /**
     * Validar todos los campos antes de enviar
     */
    private fun validateAllFields(username: String, password: String, confirmPassword: String): Boolean {
        validateUsername(username)
        validatePassword(password)
        validatePasswordConfirmation(confirmPassword, password)

        val currentState = _uiState.value
        return currentState.usernameError.isEmpty() &&
                currentState.passwordError.isEmpty() &&
                currentState.confirmPasswordError.isEmpty() &&
                username.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty()
    }

    /**
     * Limpiar error general
     */
    private fun clearGeneralError() {
        _uiState.value = _uiState.value.copy(generalError = "")
    }

    /**
     * Reset del estado
     */
    fun resetState() {
        _uiState.value = RegisterUiState()
        _username.value = ""
        _password.value = ""
        _confirmPassword.value = ""
    }
}

/**
 * Estado de UI para RegisterScreen
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val usernameError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val generalError: String = ""
) {
    // Computed properties para facilitar uso en UI
    val hasErrors: Boolean get() = usernameError.isNotEmpty() ||
            passwordError.isNotEmpty() ||
            confirmPasswordError.isNotEmpty() ||
            generalError.isNotEmpty()

    val enableRegisterButton: Boolean get() = !isLoading && !hasErrors
    val showGeneralError: Boolean get() = generalError.isNotEmpty()
}