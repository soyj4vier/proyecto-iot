package com.example.proyectoiot

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoiot.databinding.ActivityRegistroBinding
import com.example.proyectoiot.network.ApiClient
import com.example.proyectoiot.network.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registrarAdmin()
        }
    }

    private fun registrarAdmin() {
        val depto = binding.etNumDepto.text.toString().trim()
        val torre = binding.etTorre.text.toString().trim()
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (depto.isEmpty() || nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressReg.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        ApiClient.instance.registrarAdmin(depto, torre, nombre, email, pass).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                binding.progressReg.visibility = View.GONE
                binding.btnRegister.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        Toast.makeText(this@RegistroActivity, "Registro Exitoso. ¡Inicia sesión!", Toast.LENGTH_LONG).show()
                        finish() // Volver al Login
                    } else {
                        Toast.makeText(this@RegistroActivity, "Error: ${res.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@RegistroActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                binding.progressReg.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this@RegistroActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}