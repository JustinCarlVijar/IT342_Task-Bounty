package com.example.bountymobile.api

import com.example.bountymobile.AppSession
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://stripe-production-6726.up.railway.app/"

    private val jwtInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val builder = original.newBuilder()

        val jwtToken = AppSession.jwtToken
        if (!jwtToken.isNullOrEmpty()) {
            builder.addHeader("Cookie", "jwt=$jwtToken")
        }

        chain.proceed(builder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(jwtInterceptor)
        .build()

    val instance: AuthApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
}