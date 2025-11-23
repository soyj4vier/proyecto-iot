package com.example.proyectoiot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoiot.databinding.ActivityAdminBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.ControlResponse
import com.example.proyectoiot.network.HistorialResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private var userId: Int = 0
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del Intent
        userId = intent.getIntExtra("USER_ID", 0)
        userName = intent.getStringExtra("USER_NAME")

        // Configurar UI
        binding.adminTitle.text = "Hola, ${userName ?: "Admin"}"

        binding.btnOpenBarrier.setOnClickListener {
            enviarComando("ABRIR")
        }

        binding.btnCloseBarrier.setOnClickListener {
            enviarComando("CERRAR")
        }

        binding.btnManageSensors.setOnClickListener {
            val intent = Intent(this, GestionSensoresActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnManageUsers.setOnClickListener {
            val intent = Intent(this, GestionUsuariosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // Cargar el historial real desde el servidor
        cargarHistorial()
    }

    private fun cargarHistorial() {
        binding.tvLogs.text = "Cargando historial del servidor..."

        ApiClient.instance.obtenerHistorial(userId).enqueue(object : Callback<HistorialResponse> {
            override fun onResponse(call: Call<HistorialResponse>, response: Response<HistorialResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success" && res.eventos != null) {
                        val sb = StringBuilder()
                        // Agregamos una marca de tiempo actual
                        val fechaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        sb.append("[$fechaActual] Sesión iniciada y datos actualizados.\n\n")

                        for (evento in res.eventos) {
                            // Formato simple: [Fecha] Evento - Resultado
                            sb.append("[${evento.fechaHora}]\n${evento.tipoEvento} - ${evento.resultado}\n(Origen: ${evento.origen})\n\n")
                        }
                        binding.tvLogs.text = sb.toString()
                    } else {
                        binding.tvLogs.text = "Sesión iniciada.\nNo hay eventos históricos recientes."
                    }
                } else {
                    binding.tvLogs.text = "Error al cargar historial: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<HistorialResponse>, t: Throwable) {
                binding.tvLogs.text = "Error de conexión. No se pudo cargar el historial antiguo."
            }
        })
    }

    private fun enviarComando(accion: String) {
        if (userId == 0) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnOpenBarrier.isEnabled = false
        binding.btnCloseBarrier.isEnabled = false
        
        // Agregamos el log localmente mientras esperamos respuesta
        agregarLog("Enviando comando: $accion...")

        ApiClient.instance.controlarBarrera(accion, userId).enqueue(object : Callback<ControlResponse> {
            override fun onResponse(call: Call<ControlResponse>, response: Response<ControlResponse>) {
                binding.btnOpenBarrier.isEnabled = true
                binding.btnCloseBarrier.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        agregarLog("ÉXITO: ${res.mensaje}")
                        Toast.makeText(this@AdminActivity, "Barrera ${accion}DA", Toast.LENGTH_SHORT).show()
                    } else {
                        agregarLog("ERROR: ${res.mensaje}")
                    }
                } else {
                    agregarLog("Error servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ControlResponse>, t: Throwable) {
                binding.btnOpenBarrier.isEnabled = true
                binding.btnCloseBarrier.isEnabled = true
                agregarLog("Fallo de conexión: ${t.message}")
            }
        })
    }

    private fun agregarLog(mensaje: String) {
        val fecha = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logActual = binding.tvLogs.text.toString()
        // Insertamos el nuevo mensaje AL PRINCIPIO
        val nuevoLog = "[$fecha] $mensaje\n$logActual"
        binding.tvLogs.text = nuevoLog
    }
}