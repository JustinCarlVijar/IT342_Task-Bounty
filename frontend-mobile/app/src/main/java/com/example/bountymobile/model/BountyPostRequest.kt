package com.example.bountymobile.model

import java.math.BigDecimal

data class BountyPostRequest(
    val title: String,
    val description: String,
    val bountyPrice: BigDecimal
)