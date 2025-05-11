package com.example.bountymobile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    placeholder = { Text("Username or Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFBFBF), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFBFBF), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            if (identifier.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val request = LoginRequest(identifier, password)

                            scope.launch(Dispatchers.IO) {
                                try {
                                    val response = RetrofitClient.instance.login(request)

                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful && response.body()?.status == "success") {
                                            val userData = response.body()?.data

                                            AppSession.username = userData?.username
                                            AppSession.userId = userData?.userId
                                            AppSession.email = userData?.email
                                            AppSession.countryCode = userData?.countryCode
                                            AppSession.birthDate = userData?.birthDate

                                            val cookie = response.headers()["Set-Cookie"]
                                            if (cookie != null && cookie.contains("jwt=")) {
                                                AppSession.jwtToken = cookie.substringAfter("jwt=").substringBefore(";")
                                            } else {
                                                AppSession.jwtToken = null
                                            }

                                            if (AppSession.jwtToken.isNullOrEmpty()) {
                                                Toast.makeText(context, "Login failed: No token received.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            }
                                        } else {
                                            Toast.makeText(context, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Login", color = Color.White)
                    }

                    Button(
                        onClick = onRegisterClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Register", color = Color.White)
                    }
                }
            }
        }
    }
}
