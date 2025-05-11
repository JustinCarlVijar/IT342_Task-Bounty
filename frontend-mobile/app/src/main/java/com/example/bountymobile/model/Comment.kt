package com.example.bountymobile.model

data class Comment(
    val id: String,
    val bountyPostId: String,
    val parentCommentId: String?, // null for top-level comments
    val authorId: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)
