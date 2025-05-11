package com.example.bountymobile.model

data class CommentResponse(
    val id: String,
    val bountyPostId: String,
    val parentCommentId: String?,
    val authorId: String,
    val authorUsername: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)