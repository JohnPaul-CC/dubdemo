package com.example.dubium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.collectAsState
import com.example.dubium.data.repository.UserRepository
import com.example.dubium.data.storage.TokenManager
import com.example.dubium.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    username: String = "Usuario",
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        ProfileViewModel(
            userRepository = UserRepository(),
            tokenManager = TokenManager(context)
        )
    }

    // Observar estados del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val user by viewModel.user.collectAsState()

    // Color granate personalizado
    val burgundyColor = Color(0xFF8B1538)

    // Verificar validez del token al cargar
    LaunchedEffect(Unit) {
        viewModel.verifyTokenValidity {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(burgundyColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con título y botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DUBIUM",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Row {
                    // Botón refrescar
                    IconButton(
                        onClick = { viewModel.refreshProfile() },
                        enabled = uiState.enableLogout
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = Color.White
                        )
                    }

                    // Botón logout
                    IconButton(
                        onClick = {
                            viewModel.logout {
                                onNavigateToLogin()
                            }
                        },
                        enabled = uiState.enableLogout
                    ) {
                        if (uiState.isLoggingOut) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Contenido principal
            when {
                uiState.isLoading -> {
                    // Estado de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando perfil...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                uiState.showError -> {
                    // Estado de error
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage,
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refreshProfile() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = burgundyColor
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.showContent -> {
                    // Contenido principal del perfil
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Saludo personalizado
                        Text(
                            text = viewModel.getGreeting(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Card con información del usuario
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Icono de usuario
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(64.dp),
                                    tint = burgundyColor
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Información del usuario
                                viewModel.getAccountInfo().forEach { (label, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$label:",
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = value,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Black
                                        )
                                    }
                                    if (label != viewModel.getAccountInfo().keys.last()) {
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = Color.Gray.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Información adicional
                        Text(
                            text = "Bienvenido a Dubium",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    // Estado por defecto (fallback)
                    Text(
                        text = "Hola $username",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen("juan") {}
}