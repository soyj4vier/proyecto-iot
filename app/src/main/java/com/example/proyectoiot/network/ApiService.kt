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

    @GET("gestion_sensores.php")
    fun editarSensor(
        @Query("accion") accion: String = "EDITAR",
        @Query("id_usuario") idUsuario: Int,
        @Query("id_sensor") idSensor: Int,
        @Query("codigo") codigo: String,
        @Query("tipo") tipo: String,
        @Query("estado") estado: String
    ): Call<BasicResponse>

    @GET("gestion_sensores.php")
    fun eliminarSensor(
        @Query("accion") accion: String = "ELIMINAR",
        @Query("id_usuario") idUsuario: Int,
        @Query("id_sensor") idSensor: Int
    ): Call<BasicResponse>

    @GET("gestion_sensores.php")
    fun obtenerUltimoSensor(
        @Query("accion") accion: String = "OBTENER_ULTIMO",
        @Query("id_usuario") idUsuario: Int
    ): Call<BasicResponse>

    @GET("historial.php")
    fun obtenerHistorial(
        @Query("id_usuario") idUsuario: Int
    ): Call<HistorialResponse>

    // --- GESTIÓN DE USUARIOS ---
    @GET("gestion_usuarios.php")
    fun listarUsuarios(
        @Query("accion") accion: String = "LISTAR",
        @Query("id_usuario") idUsuario: Int
    ): Call<UsuarioResponse>

    @GET("gestion_usuarios.php")
    fun agregarUsuario(
        @Query("accion") accion: String = "AGREGAR",
        @Query("id_usuario") idUsuario: Int,
        @Query("nombre") nombre: String,
        @Query("email") email: String,
        @Query("password") pass: String
    ): Call<BasicResponse>

    @GET("gestion_usuarios.php")
    fun editarUsuario(
        @Query("accion") accion: String = "EDITAR",
        @Query("id_usuario") idUsuario: Int, 
        @Query("id_target") idTarget: Int,    
        @Query("nombre") nombre: String,
        @Query("email") email: String,
        @Query("estado") estado: String,
        @Query("password") pass: String
    ): Call<BasicResponse>

    @GET("gestion_usuarios.php")
    fun eliminarUsuario(
        @Query("accion") accion: String = "ELIMINAR",
        @Query("id_usuario") idUsuario: Int, 
        @Query("id_target") idTarget: Int    
    ): Call<BasicResponse>

    // --- REGISTRO INICIAL (SIGN UP) ---
    @GET("registro.php")
    fun registrarAdmin(
        @Query("numero_depto") numDepto: String,
        @Query("torre") torre: String,
        @Query("nombre") nombre: String,
        @Query("email") email: String,
        @Query("password") pass: String
    ): Call<BasicResponse>
}