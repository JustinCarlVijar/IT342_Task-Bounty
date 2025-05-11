package com.example.bountymobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.BountyPost
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val items = listOf("Home", "Search", "Create", "Bounty", "Profile")
    var selectedItem by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()
    var allPosts by remember { mutableStateOf<List<BountyPost>>(emptyList()) }
    var filteredPosts by remember { mutableStateOf<List<BountyPost>>(emptyList()) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshPosts() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getBountyPosts()
                if (response.isSuccessful) {
                    val posts = response.body()?.content ?: emptyList()
                    allPosts = posts
                    filteredPosts = posts
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun formatDate(isoString: String?): String {
        return try {
            isoString?.let {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = parser.parse(it)
                if (date != null) formatter.format(date) else "Unknown"
            } ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    LaunchedEffect(Unit) {
        refreshPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Bounties") },
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
                                "Search" -> {}
                                "Create" -> navController.navigate("create")
                                "Bounty" -> navController.navigate("bounty")
                                "Profile" -> navController.navigate("profile")
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
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    val query = it.text.trim().lowercase()
                    filteredPosts = if (query.isBlank()) {
                        allPosts
                    } else {
                        allPosts.filter { post ->
                            post.title.lowercase().contains(query) ||
                                    post.description.lowercase().contains(query)
                        }
                    }
                },
                placeholder = { Text("Search by title or description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF41644A),
                    cursorColor = Color.Black
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF41644A))
                }
            } else if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(post.title, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(post.description, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bounty: ₱${post.bountyPrice}", color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Created: ${formatDate(post.createdAt)}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                VoteButtons(
                                    postId = post.id ?: "",
                                    upvotes = post.upvotes ?: 0,
                                    downvotes = post.downvotes ?: 0,
                                    onVoted = { refreshPosts() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}