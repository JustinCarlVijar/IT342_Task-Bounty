package com.example.bountymobile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bountymobile.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun VoteButtons(
    postId: String,
    upvotes: Int,
    downvotes: Int,
    onVoted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 🧠 Local mutable vote counts
    var localUpvotes by remember { mutableStateOf(upvotes) }
    var localDownvotes by remember { mutableStateOf(downvotes) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = {
            localUpvotes += 1
            scope.launch {
                try {
                    val response = RetrofitClient.instance.voteOnBounty(postId, "upvote")
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Upvoted!", Toast.LENGTH_SHORT).show()
                        onVoted()
                    } else {
                        Toast.makeText(context, response.body() ?: "Already upvoted", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("👍 $localUpvotes")
        }

        Button(onClick = {
            localDownvotes += 1
            scope.launch {
                try {
                    val response = RetrofitClient.instance.voteOnBounty(postId, "downvote")
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Downvoted!", Toast.LENGTH_SHORT).show()
                        onVoted()
                    } else {
                        Toast.makeText(context, response.body() ?: "Already downvoted", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("👎 $localDownvotes")
        }
    }
}