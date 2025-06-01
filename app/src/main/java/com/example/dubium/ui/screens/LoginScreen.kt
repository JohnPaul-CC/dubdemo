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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubium.data.repository.UserRepository
import com.example.dubium.data.storage.TokenManager
import com.example.dubium.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: (String) -> Unit = {}
) {

    val context = LocalContext.current
    val viewModel = remember {
        LoginViewModel(
            userRepository = UserRepository(),
            tokenManager = TokenManager(context)
        )
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

            // Card de login
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
                    // Título Log in
                    Text(
                        text = "Log in",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Campo Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyColor,
                            focusedLabelColor = burgundyColor,
                            cursorColor = burgundyColor
                        )
                    )

                    // Campo Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyColor,
                            focusedLabelColor = burgundyColor,
                            cursorColor = burgundyColor
                        )
                    )

                    // Botón de login con flecha
                    Button(
                        onClick = {
                            // Simular login exitoso y navegar a home
                            onNavigateToHome(username.ifEmpty { "Usuario" })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = burgundyColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Login",
                            tint = Color.White
                        )
                    }

                    // Texto "or"
                    Text(
                        text = "or",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Botón Register
                    Button(
                        onClick = { onNavigateToRegister() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = burgundyColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToRegister = {},
        onNavigateToHome = {}
    )
}