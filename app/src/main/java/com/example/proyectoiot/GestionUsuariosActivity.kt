package com.example.proyectoiot

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoiot.databinding.ActivityGestionUsuariosBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.BasicResponse
import com.example.proyectoiot.network.Usuario
import com.example.proyectoiot.network.UsuarioResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionUsuariosBinding
    private var userId: Int = 0
    private lateinit var adapter: UsuarioAdapter
    private val usuariosList = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", 0)
        if (userId == 0) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        configurarRecyclerView()

        binding.fabAddUser.setOnClickListener {
            mostrarDialogoAgregarUsuario()
        }

        cargarUsuarios()
    }

    private fun configurarRecyclerView() {
        // Ahora pasamos las lambdas para editar y eliminar
        adapter = UsuarioAdapter(usuariosList, 
            onEditClick = { usuario -> mostrarDialogoEditarUsuario(usuario) },
            onDeleteClick = { usuario -> confirmarEliminarUsuario(usuario) }
        )
        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)
        binding.rvUsuarios.adapter = adapter
    }

    private fun cargarUsuarios() {
        binding.progressBarUsers.visibility = View.VISIBLE
        
        ApiClient.instance.listarUsuarios(idUsuario = userId).enqueue(object : Callback<UsuarioResponse> {
            override fun onResponse(call: Call<UsuarioResponse>, response: Response<UsuarioResponse>) {
                binding.progressBarUsers.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success" && res.usuarios != null) {
                        adapter.actualizarDatos(res.usuarios)
                        if (res.usuarios.isEmpty()) {
                            Toast.makeText(this@GestionUsuariosActivity, "No hay usuarios registrados", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@GestionUsuariosActivity, res.mensaje ?: "Error al cargar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GestionUsuariosActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UsuarioResponse>, t: Throwable) {
                binding.progressBarUsers.visibility = View.GONE
                Toast.makeText(this@GestionUsuariosActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // --- AGREGAR USUARIO ---
    private fun mostrarDialogoAgregarUsuario() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Operador")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre completo"
        layout.addView(inputNombre)

        val inputEmail = EditText(this)
        inputEmail.hint = "Email"
        layout.addView(inputEmail)

        val inputPassword = EditText(this)
        inputPassword.hint = "Contraseña"
        layout.addView(inputPassword)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                guardarUsuario(nombre, email, password)
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun guardarUsuario(nombre: String, email: String, pass: String) {
        binding.progressBarUsers.visibility = View.VISIBLE
        ApiClient.instance.agregarUsuario(idUsuario = userId, nombre = nombre, email = email, pass = pass)
            .enqueue(manejarRespuestaBasic("Usuario creado"))
    }

    // --- EDITAR USUARIO ---
    private fun mostrarDialogoEditarUsuario(usuario: Usuario) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Operador")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre"
        inputNombre.setText(usuario.nombre)
        layout.addView(inputNombre)

        val inputEmail = EditText(this)
        inputEmail.hint = "Email"
        inputEmail.setText(usuario.email)
        layout.addView(inputEmail)

        val inputPassword = EditText(this)
        inputPassword.hint = "Nueva Contraseña (dejar vacío para no cambiar)"
        layout.addView(inputPassword)
        
        // Selector de Estado
        val spinnerEstado = Spinner(this)
        val estados = arrayOf("ACTIVO", "INACTIVO", "BLOQUEADO")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerEstado.adapter = adapterSpinner
        
        // Seleccionar estado actual
        val estadoIndex = estados.indexOf(usuario.estado)
        if (estadoIndex >= 0) spinnerEstado.setSelection(estadoIndex)
        
        layout.addView(spinnerEstado)

        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val nombre = inputNombre.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val estado = spinnerEstado.selectedItem.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty()) {
                editarUsuario(usuario.idUsuario, nombre, email, estado, password)
            } else {
                Toast.makeText(this, "Nombre e Email son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun editarUsuario(idTarget: Int, nombre: String, email: String, estado: String, pass: String) {
        binding.progressBarUsers.visibility = View.VISIBLE
        ApiClient.instance.editarUsuario(
            idUsuario = userId,
            idTarget = idTarget,
            nombre = nombre,
            email = email,
            estado = estado,
            pass = pass
        ).enqueue(manejarRespuestaBasic("Usuario actualizado"))
    }

    // --- ELIMINAR USUARIO ---
    private fun confirmarEliminarUsuario(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar a ${usuario.nombre}?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarUsuario(usuario.idUsuario)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarUsuario(idTarget: Int) {
        binding.progressBarUsers.visibility = View.VISIBLE
        ApiClient.instance.eliminarUsuario(idUsuario = userId, idTarget = idTarget)
            .enqueue(manejarRespuestaBasic("Usuario eliminado"))
    }

    // --- HELPER PARA RESPUESTAS SIMPLES ---
    private fun manejarRespuestaBasic(mensajeExito: String): Callback<BasicResponse> {
        return object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        Toast.makeText(this@GestionUsuariosActivity, mensajeExito, Toast.LENGTH_SHORT).show()
                        cargarUsuarios() // Recargar lista
                    } else {
                        binding.progressBarUsers.visibility = View.GONE
                        Toast.makeText(this@GestionUsuariosActivity, "Error: ${res.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    binding.progressBarUsers.visibility = View.GONE
                    Toast.makeText(this@GestionUsuariosActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                binding.progressBarUsers.visibility = View.GONE
                Toast.makeText(this@GestionUsuariosActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}