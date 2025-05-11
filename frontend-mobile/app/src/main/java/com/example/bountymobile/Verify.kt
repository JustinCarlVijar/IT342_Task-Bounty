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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bountymobile.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Verify(
    navController: NavHostController,
    username: String,   // ✅ can be ignored
    jwtToken: String    // ✅ can be ignored
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }

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
                Text("Email Verification", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = { Text("Enter Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFBFBF), RoundedCornerShape(50)),
                    shape = RoundedCornerShape(50),
                    visualTransformation = VisualTransformation.None,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Back", color = Color.White)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (code.length == 8 && code.all { it.isDigit() }) {
                                    try {
                                        RetrofitClient.instance.verifyEmail(code.toLong())
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Email verified!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("register") { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter a valid code.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Verify", color = Color.White)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    RetrofitClient.instance.resendVerificationCode()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Code resent successfully.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Resend failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Resend", color = Color.White)
                    }
                }
            }
        }
    }
}
