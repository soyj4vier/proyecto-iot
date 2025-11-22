package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class ControlResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("accion_registrada") val accionRegistrada: String?
)