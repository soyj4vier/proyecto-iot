package com.example.proyectoiot

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoiot.databinding.ActivityGestionSensoresBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.BasicResponse
import com.example.proyectoiot.network.Sensor
import com.example.proyectoiot.network.SensorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.LinearLayout

class GestionSensoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionSensoresBinding
    private var userId: Int = 0
    private lateinit var adapter: SensorAdapter
    private val sensoresList = mutableListOf<Sensor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionSensoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", 0)
        if (userId == 0) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        configurarRecyclerView()

        binding.fabAddSensor.setOnClickListener {
            mostrarDialogoAgregar()
        }

        cargarSensores()
    }

    private fun configurarRecyclerView() {
        adapter = SensorAdapter(sensoresList)
        binding.rvSensores.layoutManager = LinearLayoutManager(this)
        binding.rvSensores.adapter = adapter
    }

    private fun cargarSensores() {
        binding.progressBarSensors.visibility = View.VISIBLE
        
        ApiClient.instance.listarSensores(idUsuario = userId).enqueue(object : Callback<SensorResponse> {
            override fun onResponse(call: Call<SensorResponse>, response: Response<SensorResponse>) {
                binding.progressBarSensors.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success" && res.sensores != null) {
                        adapter.actualizarDatos(res.sensores)
                        if (res.sensores.isEmpty()) {
                            Toast.makeText(this@GestionSensoresActivity, "No hay sensores registrados", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@GestionSensoresActivity, res.mensaje ?: "Error al cargar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GestionSensoresActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SensorResponse>, t: Throwable) {
                binding.progressBarSensors.visibility = View.GONE
                Toast.makeText(this@GestionSensoresActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun mostrarDialogoAgregar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Sensor")

        // Crear layout programáticamente para el diálogo
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputCodigo = EditText(this)
        inputCodigo.hint = "Código UID (Ej: A1 B2 C3 D4)"
        layout.addView(inputCodigo)

        // Selector de Tipo
        val spinnerTipo = Spinner(this)
        val tipos = arrayOf("TARJETA", "LLAVERO")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerTipo.adapter = adapterSpinner
        layout.addView(spinnerTipo)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val codigo = inputCodigo.text.toString().trim()
            val tipo = spinnerTipo.selectedItem.toString()

            if (codigo.isNotEmpty()) {
                guardarSensor(codigo, tipo)
            } else {
                Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun guardarSensor(codigo: String, tipo: String) {
        binding.progressBarSensors.visibility = View.VISIBLE
        
        ApiClient.instance.agregarSensor(idUsuario = userId, codigo = codigo, tipo = tipo).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                // No ocultamos progressBar aquí porque cargarSensores() lo usará de nuevo
                
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        Toast.makeText(this@GestionSensoresActivity, "Guardado: ${res.mensaje}", Toast.LENGTH_SHORT).show()
                        cargarSensores() // Recargar la lista
                    } else {
                        binding.progressBarSensors.visibility = View.GONE
                        Toast.makeText(this@GestionSensoresActivity, "Error: ${res.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    binding.progressBarSensors.visibility = View.GONE
                    Toast.makeText(this@GestionSensoresActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                binding.progressBarSensors.visibility = View.GONE
                Toast.makeText(this@GestionSensoresActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}