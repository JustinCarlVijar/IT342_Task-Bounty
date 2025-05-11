package com.example.bountymobile.model

import java.math.BigDecimal

data class BountyPost(
    val id: String? = null,
    val creatorId: String? = null,
    val title: String,
    val description: String,
    val bountyPrice: BigDecimal,
    val isPublic: Boolean? = null,
    val upvotes: Int? = 0,
    val downvotes: Int? = 0,
    val votedUp: List<String>? = null,
    val votedDown: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)