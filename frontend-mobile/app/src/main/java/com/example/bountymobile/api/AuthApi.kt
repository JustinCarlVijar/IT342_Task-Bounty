package com.example.bountymobile.api

import com.example.bountymobile.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // --- AUTH ---
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
        @Query("code") code: Long
    ): Response<ResponseBody>

    @POST("/auth/resend_code")
    suspend fun resendVerificationCode(): Response<ResponseBody>


    // --- BOUNTY POSTS ---
    @POST("/bounty_post")
    suspend fun createBountyPost(
        @Body bountyPostRequest: BountyPostRequest
    ): Response<BountyPost>

    @GET("/bounty_post")
    suspend fun getBountyPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 25,
        @Query("sortBy") sortBy: String = "most_upvoted",
        @Query("search") search: String? = null
    ): Response<BountyPostPageResponse>

    @POST("/bounty_post/{id}/vote")
    suspend fun voteOnBounty(
        @Path("id") bountyId: String,
        @Query("type") type: String
    ): Response<String>


    // --- STRIPE ---
    @GET("/stripe/checkout/{bountyPostId}")
    suspend fun createCheckoutSession(
        @Path("bountyPostId") bountyPostId: String
    ): Response<ResponseBody>


    // --- COMMENTS ---
    @GET("/comment/{postId}/bounty_post")
    suspend fun getComments(
        @Path("postId") bountyPostId: String
    ): Response<List<CommentResponse>>

    @POST("/comment/{postId}/bounty_post")
    suspend fun postComment(
        @Path("postId") bountyPostId: String,
        @Body commentRequest: CommentRequest
    ): Response<Comment>

    @DELETE("/bounty_post/{id}")
    suspend fun deleteBountyPost(
        @Path("id") bountyPostId: String
    ): Response<String>

}