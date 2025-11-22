package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class HistorialResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("eventos") val eventos: List<Evento>?
)