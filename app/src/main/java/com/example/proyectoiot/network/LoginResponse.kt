package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("rol") val rol: String?, // "ADMINISTRADOR" o "OPERADOR"
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("id_usuario") val idUsuario: Int?
)