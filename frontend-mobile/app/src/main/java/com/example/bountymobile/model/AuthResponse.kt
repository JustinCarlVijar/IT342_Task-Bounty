package com.example.bountymobile.model

data class AuthResponse(
    val status: String,
    val data: AuthData?,           // Nullable for error cases
    val message: String? = null    // May contain error or success messages
)

data class AuthData(
    val userId: String?,
    val username: String?,
    val email: String? = null,
    val birthDate: String? = null,
    val countryCode: String? = null,
    val message: String? = null
)
