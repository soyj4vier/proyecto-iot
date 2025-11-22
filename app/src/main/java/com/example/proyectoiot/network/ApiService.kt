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

    @GET("control_barrera.php")
    fun controlarBarrera(
        @Query("accion") accion: String, // "ABRIR" o "CERRAR"
        @Query("id_usuario") idUsuario: Int
    ): Call<ControlResponse>

    @GET("gestion_sensores.php")
    fun listarSensores(
        @Query("accion") accion: String = "LISTAR",
        @Query("id_usuario") idUsuario: Int
    ): Call<SensorResponse>

    @GET("gestion_sensores.php")
    fun agregarSensor(
        @Query("accion") accion: String = "AGREGAR",
        @Query("id_usuario") idUsuario: Int,
        @Query("codigo") codigo: String,
        @Query("tipo") tipo: String // "TARJETA" o "LLAVERO"
    ): Call<BasicResponse>

    @GET("historial.php")
    fun obtenerHistorial(
        @Query("id_usuario") idUsuario: Int
    ): Call<HistorialResponse>
}