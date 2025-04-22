package com.example.bountymobile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val items = listOf("Home", "Search", "Create", "Bounty", "Profile")
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Bounty") },
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
                            if (label == "Profile") {
                                navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Main Page", color = Color.Gray)
        }
    }
}