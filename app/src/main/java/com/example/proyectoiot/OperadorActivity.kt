package com.example.proyectoiot

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoiot.databinding.ActivityOperadorBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.ControlResponse
import com.example.proyectoiot.network.Evento
import com.example.proyectoiot.network.HistorialResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OperadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperadorBinding
    private var userId: Int = 0
    private var userName: String? = null
    private lateinit var adapter: HistorialAdapter
    private val historialList = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos
        userId = intent.getIntExtra("USER_ID", 0)
        userName = intent.getStringExtra("USER_NAME")

        // Configurar UI
        binding.tvWelcome.text = "Hola, ${userName ?: "Operador"}"
        
        configurarRecyclerView()

        // Lógica Botón Abrir
        binding.btnOpen.setOnClickListener {
            abrirBarrera()
        }

        // Lógica SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener {
            cargarHistorial()
        }

        // Cargar datos iniciales
        cargarHistorial()
    }

    private fun configurarRecyclerView() {
        adapter = HistorialAdapter(historialList)
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = adapter
    }

    private fun abrirBarrera() {
        if (userId == 0) return

        binding.btnOpen.isEnabled = false
        binding.btnOpen.text = "ABRIENDO..."

        ApiClient.instance.controlarBarrera("ABRIR", userId).enqueue(object : Callback<ControlResponse> {
            override fun onResponse(call: Call<ControlResponse>, response: Response<ControlResponse>) {
                binding.btnOpen.isEnabled = true
                binding.btnOpen.text = "ABRIR BARRERA"

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        Toast.makeText(this@OperadorActivity, "¡Barrera Abierta!", Toast.LENGTH_SHORT).show()
                        // Recargar historial para ver el nuevo evento
                        cargarHistorial()
                    } else {
                        Toast.makeText(this@OperadorActivity, res.mensaje ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@OperadorActivity, "Error servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ControlResponse>, t: Throwable) {
                binding.btnOpen.isEnabled = true
                binding.btnOpen.text = "ABRIR BARRERA"
                Toast.makeText(this@OperadorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarHistorial() {
        binding.swipeRefresh.isRefreshing = true

        ApiClient.instance.obtenerHistorial(userId).enqueue(object : Callback<HistorialResponse> {
            override fun onResponse(call: Call<HistorialResponse>, response: Response<HistorialResponse>) {
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success" && res.eventos != null) {
                        adapter.actualizarDatos(res.eventos)
                    } else {
                        // Si no hay eventos o hay error, lista vacía
                        adapter.actualizarDatos(emptyList())
                    }
                }
            }

            override fun onFailure(call: Call<HistorialResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@OperadorActivity, "No se pudo cargar historial", Toast.LENGTH_SHORT).show()
            }
        })
    }
}