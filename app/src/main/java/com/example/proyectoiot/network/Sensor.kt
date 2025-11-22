package com.example.proyectoiot.network

import com.google.gson.annotations.SerializedName

data class Sensor(
    @SerializedName("id_sensor") val idSensor: Int,
    @SerializedName("codigo_sensor") val codigo: String,
    @SerializedName("tipo") val tipo: String, // TARJETA o LLAVERO
    @SerializedName("estado") val estado: String, // ACTIVO, INACTIVO, BLOQUEADO
    @SerializedName("fecha_alta") val fechaAlta: String
)