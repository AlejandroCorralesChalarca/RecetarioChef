package com.example.chefapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chefapp.databinding.ActivityRegistroBinding
import com.example.chefapp.viewmodel.RegistroViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: RegistroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnRegistrarCuenta.setOnClickListener {
            val nombre = binding.editNombreRegistro.text.toString()
            val email = binding.editEmailRegistro.text.toString()
            val telefono = binding.editTelefonoRegistro.text.toString()
            val restaurante = binding.editRestauranteRegistro.text.toString()
            val direccion = binding.editDireccionRegistro.text.toString()
            val pass = binding.editPasswordRegistro.text.toString()
            
            if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && 
                telefono.isNotEmpty() && restaurante.isNotEmpty() && direccion.isNotEmpty()) {
                viewModel.registrar(nombre, email, telefono, restaurante, direccion, pass)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.registroState.observe(this) { state ->
            when (state) {
                is RegistroViewModel.RegistroState.Loading -> {
                    binding.progressBarRegistro.visibility = View.VISIBLE
                    binding.btnRegistrarCuenta.isEnabled = false
                }
                is RegistroViewModel.RegistroState.Success -> {
                    binding.progressBarRegistro.visibility = View.GONE
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is RegistroViewModel.RegistroState.Error -> {
                    binding.progressBarRegistro.visibility = View.GONE
                    binding.btnRegistrarCuenta.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
