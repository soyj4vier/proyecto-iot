package com.example.proyectoiot.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("login.php")
    fun login(
        @Query("email") email: String,
        @Query("password") pass: String
    ): Call<LoginResponse>
}