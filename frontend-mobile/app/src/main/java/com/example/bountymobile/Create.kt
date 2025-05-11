package com.example.bountymobile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.BountyPostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Create(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bountyPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Bounty") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF41644A), titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = bountyPrice,
                onValueChange = { bountyPrice = it },
                label = { Text("Bounty Price") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || bountyPrice.isBlank()) {
                        Toast.makeText(context, "All fields must be filled.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val bountyPriceDecimal = bountyPrice.toBigDecimalOrNull()
                    if (bountyPriceDecimal == null || bountyPriceDecimal <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Bounty price must be a valid positive number.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (AppSession.jwtToken.isNullOrEmpty()) {
                        Toast.makeText(context, "You must be logged in first.", Toast.LENGTH_LONG).show()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                        return@Button
                    }

                    val bountyPostRequest = BountyPostRequest(title, description, bountyPriceDecimal)

                    scope.launch(Dispatchers.IO) {
                        try {
                            val response = RetrofitClient.instance.createBountyPost(bountyPostRequest)
                            if (response.isSuccessful) {
                                val post = response.body()
                                val checkoutResponse = RetrofitClient.instance.createCheckoutSession(post!!.id!!)
                                if (checkoutResponse.isSuccessful) {
                                    val sessionUrl = checkoutResponse.body()?.string() ?: ""
                                    withContext(Dispatchers.Main) {
                                        if (sessionUrl.isNotBlank()) {
                                            openCustomTab(context, sessionUrl)
                                            navController.navigate("bounty") {
                                                popUpTo("main") { inclusive = false }
                                            }
                                        } else {
                                            Toast.makeText(context, "Stripe URL is empty", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    val err = checkoutResponse.errorBody()?.string() ?: "Unknown Stripe error"
                                    Log.e("StripeCheckout", "Stripe failed: $err")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Stripe error: $err", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                val msg = when (response.code()) {
                                    401 -> "You must be logged in to create a bounty."
                                    403 -> "Access denied. Please check your credentials."
                                    else -> response.errorBody()?.string() ?: "Failed to create bounty."
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (response.code() == 401) {
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CreateBounty", "Exception: ${e.localizedMessage}", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Post Bounty", color = Color.White)
            }
        }
    }
}

fun openCustomTab(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}