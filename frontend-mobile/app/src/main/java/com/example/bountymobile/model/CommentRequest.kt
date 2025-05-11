package com.example.bountymobile.model

data class CommentRequest(
    val parentCommentId: String? = null,
    val content: String
)