package com.example.bountymobile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bountymobile.api.RetrofitClient
import com.example.bountymobile.model.CommentRequest
import com.example.bountymobile.model.CommentResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(navController: NavController, bountyPostId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadComments() {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getComments(bountyPostId)
                if (response.isSuccessful) {
                    comments = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
                    Log.e("CommentsScreen", "Load failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("CommentsScreen", "Exception loading comments", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun submitComment() {
        val request = CommentRequest(content = newComment)
        scope.launch {
            try {
                val response = RetrofitClient.instance.postComment(
                    bountyPostId = bountyPostId,
                    commentRequest = request
                )
                if (response.isSuccessful) {
                    newComment = ""
                    loadComments()
                } else {
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                    Log.e("CommentsScreen", "Post failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("CommentsScreen", "Exception posting comment", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        loadComments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newComment,
                onValueChange = { newComment = it },
                label = { Text("Add a comment...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { submitComment() },
                enabled = newComment.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Post")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(comments) { comment ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = comment.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "By: ${comment.authorUsername}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}