package com.example.bountymobile.model

data class AuthResponse(
    val status: String,
    val data: AuthData?,
    val message: String? = null
)

data class AuthData(
    val userId: String?,
    val username: String?,
    val email: String? = null,
    val birthDate: String? = null,
    val countryCode: String? = null,
    val message: String? = null
)