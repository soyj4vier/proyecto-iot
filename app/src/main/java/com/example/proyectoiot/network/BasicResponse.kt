package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?
)