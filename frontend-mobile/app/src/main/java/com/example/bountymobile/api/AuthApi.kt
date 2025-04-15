package com.example.bountymobile.api

import com.example.bountymobile.model.AuthResponse
import com.example.bountymobile.model.LoginRequest
import com.example.bountymobile.model.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    @POST("/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<AuthResponse>

    @POST("/auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<AuthResponse>

    @POST("/auth/verify")
    suspend fun verifyEmail(
        @Query("code") code: Long,
        @Header("Cookie") cookie: String
    ): Response<ResponseBody>

    @POST("/auth/resend_code")
    suspend fun resendVerificationCode(
        @Header("Cookie") cookie: String
    ): Response<ResponseBody>
}