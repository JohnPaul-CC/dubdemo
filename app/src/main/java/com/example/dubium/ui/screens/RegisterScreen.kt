@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dubium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.dubium.data.repository.UserRepository
import com.example.dubium.data.storage.TokenManager
import com.example.dubium.ui.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        RegisterViewModel(
            userRepository = UserRepository(),
            tokenManager = TokenManager(context)
        )
    }

    // Observar estados del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    // Color granate personalizado
    val burgundyColor = Color(0xFF8B1538)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(burgundyColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título DUBIUM
            Text(
                text = "DUBIUM",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 80.dp)
            )

            // Card de registro
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título Register
                    Text(
                        text = "Register",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Mensaje de error general si existe
                    if (uiState.showGeneralError) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.generalError,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Campo Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = viewModel::updateUsername,
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyColor,
                            focusedLabelColor = burgundyColor,
                            cursorColor = burgundyColor,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        enabled = !uiState.isLoading,
                        isError = uiState.usernameError.isNotEmpty(),
                        supportingText = if (uiState.usernameError.isNotEmpty()) {
                            { Text(
                                text = uiState.usernameError,
                                color = Color.Red,
                                fontSize = 12.sp
                            ) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyColor,
                            focusedLabelColor = burgundyColor,
                            cursorColor = burgundyColor,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        enabled = !uiState.isLoading,
                        isError = uiState.passwordError.isNotEmpty(),
                        supportingText = if (uiState.passwordError.isNotEmpty()) {
                            { Text(
                                text = uiState.passwordError,
                                color = Color.Red,
                                fontSize = 12.sp
                            ) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyColor,
                            focusedLabelColor = burgundyColor,
                            cursorColor = burgundyColor,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        enabled = !uiState.isLoading,
                        isError = uiState.confirmPasswordError.isNotEmpty(),
                        supportingText = if (uiState.confirmPasswordError.isNotEmpty()) {
                            { Text(
                                text = uiState.confirmPasswordError,
                                color = Color.Red,
                                fontSize = 12.sp
                            ) }
                        } else null
                    )

                    // Botón de registro con flecha
                    Button(
                        onClick = {
                            viewModel.register { registeredUsername ->
                                onNavigateToHome(registeredUsername)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = burgundyColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = uiState.enableRegisterButton
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Register",
                                tint = Color.White
                            )
                        }
                    }

                    // Botón para volver al login
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { onNavigateToLogin() },
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión",
                            color = burgundyColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateToLogin = {},
        onNavigateToHome = {}
    )
}