package com.example.bountymobile.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val birthDate: String,
    val countryCode: String
)
