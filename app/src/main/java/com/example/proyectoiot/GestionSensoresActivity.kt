package com.example.proyectoiot

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
        adapter = SensorAdapter(sensoresList,
            onEditClick = { sensor -> mostrarDialogoEditarSensor(sensor) },
            onDeleteClick = { sensor -> confirmarEliminarSensor(sensor) }
        )
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

    // --- HELPER: ESCANEAR DESDE HARDWARE ---
    private fun obtenerUltimoCodigoLeido(editText: EditText) {
        Toast.makeText(this, "Consultando hardware...", Toast.LENGTH_SHORT).show()
        ApiClient.instance.obtenerUltimoSensor(idUsuario = userId).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        editText.setText(res.mensaje) // El mensaje trae el UID
                        Toast.makeText(this@GestionSensoresActivity, "Código obtenido!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@GestionSensoresActivity, "No hay lecturas recientes", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GestionSensoresActivity, "Error al leer último sensor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                Toast.makeText(this@GestionSensoresActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- AGREGAR SENSOR ---
    private fun mostrarDialogoAgregar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Sensor")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Layout horizontal para Input + Botón Escanear
        val layoutCodigo = LinearLayout(this)
        layoutCodigo.orientation = LinearLayout.HORIZONTAL
        
        val inputCodigo = EditText(this)
        inputCodigo.hint = "Código UID (Ej: A1B2C3D4)"
        val paramsInput = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        inputCodigo.layoutParams = paramsInput
        layoutCodigo.addView(inputCodigo)

        val btnScan = Button(this)
        btnScan.text = "Escanear"
        btnScan.textSize = 12f
        btnScan.setOnClickListener { obtenerUltimoCodigoLeido(inputCodigo) }
        layoutCodigo.addView(btnScan)

        layout.addView(layoutCodigo)

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
                Toast.makeText(this, "El código es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun guardarSensor(codigo: String, tipo: String) {
        binding.progressBarSensors.visibility = View.VISIBLE
        ApiClient.instance.agregarSensor(idUsuario = userId, codigo = codigo, tipo = tipo)
            .enqueue(manejarRespuestaBasic("Sensor registrado"))
    }

    // --- EDITAR SENSOR ---
    private fun mostrarDialogoEditarSensor(sensor: Sensor) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Sensor")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val layoutCodigo = LinearLayout(this)
        layoutCodigo.orientation = LinearLayout.HORIZONTAL
        
        val inputCodigo = EditText(this)
        inputCodigo.hint = "Código UID"
        inputCodigo.setText(sensor.codigo)
        val paramsInput = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        inputCodigo.layoutParams = paramsInput
        layoutCodigo.addView(inputCodigo)

        val btnScan = Button(this)
        btnScan.text = "Escanear"
        btnScan.textSize = 12f
        btnScan.setOnClickListener { obtenerUltimoCodigoLeido(inputCodigo) }
        layoutCodigo.addView(btnScan)

        layout.addView(layoutCodigo)

        // Selector de Tipo
        val spinnerTipo = Spinner(this)
        val tipos = arrayOf("TARJETA", "LLAVERO")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerTipo.adapter = adapterSpinner
        val tipoIndex = tipos.indexOf(sensor.tipo)
        if (tipoIndex >= 0) spinnerTipo.setSelection(tipoIndex)
        layout.addView(spinnerTipo)

        // Selector de Estado
        val spinnerEstado = Spinner(this)
        val estados = arrayOf("ACTIVO", "INACTIVO", "PERDIDO", "BLOQUEADO")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerEstado.adapter = adapterEstado
        val estadoIndex = estados.indexOf(sensor.estado)
        if (estadoIndex >= 0) spinnerEstado.setSelection(estadoIndex)
        layout.addView(spinnerEstado)

        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val codigo = inputCodigo.text.toString().trim()
            val tipo = spinnerTipo.selectedItem.toString()
            val estado = spinnerEstado.selectedItem.toString()

            if (codigo.isNotEmpty()) {
                editarSensor(sensor.idSensor, codigo, tipo, estado)
            } else {
                Toast.makeText(this, "El código es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun editarSensor(idSensor: Int, codigo: String, tipo: String, estado: String) {
        binding.progressBarSensors.visibility = View.VISIBLE
        ApiClient.instance.editarSensor(
            idUsuario = userId,
            idSensor = idSensor,
            codigo = codigo,
            tipo = tipo,
            estado = estado
        ).enqueue(manejarRespuestaBasic("Sensor actualizado"))
    }

    // --- ELIMINAR SENSOR ---
    private fun confirmarEliminarSensor(sensor: Sensor) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Sensor")
            .setMessage("¿Estás seguro de eliminar el sensor ${sensor.codigo}?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarSensor(sensor.idSensor)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarSensor(idSensor: Int) {
        binding.progressBarSensors.visibility = View.VISIBLE
        ApiClient.instance.eliminarSensor(idUsuario = userId, idSensor = idSensor)
            .enqueue(manejarRespuestaBasic("Sensor eliminado"))
    }

    // --- HELPER RESPUESTAS ---
    private fun manejarRespuestaBasic(mensajeExito: String): Callback<BasicResponse> {
        return object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        Toast.makeText(this@GestionSensoresActivity, mensajeExito, Toast.LENGTH_SHORT).show()
                        cargarSensores() // Recargar lista
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
        }
    }
}