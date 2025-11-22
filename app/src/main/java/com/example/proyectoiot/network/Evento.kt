package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class Evento(
    @SerializedName("id_evento") val idEvento: Int,
    @SerializedName("tipo_evento") val tipoEvento: String, // ACCESO_VALIDO, APERTURA_MANUAL...
    @SerializedName("fecha_hora") val fechaHora: String,
    @SerializedName("resultado") val resultado: String, // PERMITIDO, DENEGADO
    @SerializedName("origen") val origen: String // El UID o "Manual"
)