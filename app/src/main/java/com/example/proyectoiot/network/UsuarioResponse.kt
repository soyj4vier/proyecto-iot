package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class UsuarioResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("usuarios") val usuarios: List<Usuario>?
)