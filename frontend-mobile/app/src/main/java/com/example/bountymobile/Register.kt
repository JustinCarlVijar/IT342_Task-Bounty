package com.example.bountymobile

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.RegisterRequest
import kotlinx.coroutines.*
import java.util.*

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: (username: String, jwtToken: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var countryCode by remember { mutableStateOf("PH") }
    val countryList = listOf("PH", "US", "UK", "JP", "KR")
    var expanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                birthDate = "$year-${"%02d".format(month + 1)}-${"%02d".format(day)}"
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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
                Text("Register", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "Username" to username,
                    "Email" to email,
                    "Password" to password,
                    "Confirm Password" to confirmPassword
                ).forEachIndexed { index, (label, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            when (index) {
                                0 -> username = it
                                1 -> email = it
                                2 -> password = it
                                3 -> confirmPassword = it
                            }
                        },
                        placeholder = { Text(label) },
                        visualTransformation = if (index >= 2) PasswordVisualTransformation() else VisualTransformation.None,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFBFBFBF), RoundedCornerShape(50)),
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFBFBF), RoundedCornerShape(50))
                        .clickable { showDatePicker = true }
                        .padding(vertical = 16.dp, horizontal = 20.dp)
                ) {
                    Text(
                        text = if (birthDate.isNotBlank()) birthDate else "Birthdate (YYYY-MM-DD)",
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFBFBFBF), RoundedCornerShape(50))
                            .clickable { expanded = true }
                            .padding(vertical = 16.dp, horizontal = 20.dp)
                    ) {
                        Text(text = countryCode, color = Color.DarkGray)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        countryList.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    countryCode = code
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onBackClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Back", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (username.isBlank() || email.isBlank() || password.isBlank() ||
                                confirmPassword.isBlank() || birthDate.isBlank()
                            ) {
                                Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val request = RegisterRequest(
                                username = username,
                                email = email,
                                password = password,
                                birthDate = birthDate,
                                countryCode = countryCode
                            )

                            scope.launch(Dispatchers.IO) {
                                try {
                                    val response = RetrofitClient.instance.register(request)
                                    val token = response.headers()["Set-Cookie"]?.substringBefore(";")?.removePrefix("jwt=") ?: ""

                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful && response.body()?.status == "success") {
                                            val userData = response.body()?.data
                                            AppSession.username = username
                                            AppSession.jwtToken = token
                                            AppSession.userId = userData?.userId

                                            // ✅ Save user inputs to display in profile
                                            AppSession.email = email
                                            AppSession.birthDate = birthDate
                                            AppSession.countryCode = countryCode

                                            Toast.makeText(context, "Registered successfully! Please verify your email.", Toast.LENGTH_SHORT).show()
                                            onRegisterSuccess(username, token)
                                        } else {
                                            val errorMsg = response.errorBody()?.string()
                                            if (errorMsg?.contains("not verified") == true) {
                                                Toast.makeText(context, "A new verification code was sent to your email.", Toast.LENGTH_SHORT).show()
                                                onRegisterSuccess(username, token)
                                            } else {
                                                Toast.makeText(context, response.body()?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                                            }
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
                        Text("Sign Up", color = Color.White)
                    }
                }
            }
        }
    }
}