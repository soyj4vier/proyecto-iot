package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class SensorResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("sensores") val sensores: List<Sensor>?
)