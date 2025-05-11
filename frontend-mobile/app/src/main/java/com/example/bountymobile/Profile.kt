package com.example.bountymobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavController) {
    val items = listOf("Home", "Search", "Create", "Bounty", "Profile")
    var selectedItem by remember { mutableIntStateOf(4) }
    val username = AppSession.username ?: "Unknown"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Bounty") },
                actions = {
                    Button(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
                    ) {
                        Text("Log Out", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF41644A),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF0F0F0)) {
                items.forEachIndexed { index, label ->
                    val icon = when (label) {
                        "Home" -> Icons.Default.Home
                        "Search" -> Icons.Default.Search
                        "Create" -> Icons.Default.Add
                        "Bounty" -> Icons.Default.Schedule
                        "Profile" -> Icons.Default.Person
                        else -> Icons.Default.Home
                    }

                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (label) {
                                "Home" -> navController.navigate("main")
                                "Search" -> navController.navigate("search")
                                "Create" -> navController.navigate("create")
                                "Bounty" -> navController.navigate("bounty")
                                "Profile" -> {} // Already here
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Avatar",
                tint = Color(0xFF41644A),
                modifier = Modifier
                    .size(96.dp)
                    .alpha(0.9f)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Username", fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )
                }
            }

            Text(
                text = "Welcome to your profile!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}