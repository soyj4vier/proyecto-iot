package com.example.proyectoiot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoiot.databinding.ActivityMainBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar ProgressBar y bloquear botón
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        // Llamada a la API
        ApiClient.instance.login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    if (loginResponse.status == "success") {
                        Toast.makeText(this@MainActivity, "Bienvenido ${loginResponse.nombre}", Toast.LENGTH_SHORT).show()
                        navigateBasedOnRole(loginResponse.rol)
                    } else {
                        Toast.makeText(this@MainActivity, loginResponse.mensaje ?: "Error de credenciales", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error en el servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                Toast.makeText(this@MainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun navigateBasedOnRole(rol: String?) {
        val intent = when (rol) {
            "ADMINISTRADOR" -> Intent(this, AdminActivity::class.java)
            "OPERADOR" -> Intent(this, OperadorActivity::class.java)
            else -> {
                Toast.makeText(this, "Rol desconocido: $rol", Toast.LENGTH_LONG).show()
                return
            }
        }
        // Evitar que el usuario vuelva al login con el botón 'Atrás'
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}