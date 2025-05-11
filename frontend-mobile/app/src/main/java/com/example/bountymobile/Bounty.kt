package com.example.bountymobile

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.BountyPost
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BountyScreen(navController: NavController) {
    val items = listOf("Home", "Search", "Create", "Bounty", "Profile")
    var selectedItem by remember { mutableStateOf(3) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var bountyPosts by remember { mutableStateOf<List<BountyPost>>(emptyList()) }
    val hiddenPostIds = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = AppSession.userId

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

    fun refreshPosts() {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getBountyPosts()
                if (response.isSuccessful) {
                    val allPosts = response.body()?.content ?: emptyList()
                    bountyPosts = allPosts  // Show ALL posts, not just mine
                    hiddenPostIds.clear()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bounties") },
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
                                "Bounty" -> {}
                                "Profile" -> navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF41644A))
            }
        } else {
            val visiblePosts = bountyPosts.filter { it.id != null && it.id !in hiddenPostIds }

            if (visiblePosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("You haven't created any bounties yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(innerPadding)
                ) {
                    items(visiblePosts, key = { it.id ?: UUID.randomUUID().toString() }) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black
                                    )
                                    TextButton(
                                        onClick = {
                                            val postId = post.id
                                            if (postId != null) {
                                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                                hiddenPostIds.add(postId)
                                            } else {
                                                Toast.makeText(context, "Missing ID!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("🗑️")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(post.description, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bounty: ₱${post.bountyPrice}", color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Created: ${formatDate(post.createdAt)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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