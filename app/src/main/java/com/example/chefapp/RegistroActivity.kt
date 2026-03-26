package com.example.chefapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chefapp.databinding.ActivityRegistroBinding

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
            val pass = binding.editPasswordRegistro.text.toString()
            viewModel.registrar(nombre, email, pass)
        }

        binding.txtVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.registroState.observe(this) { state ->

            binding.btnRegistrarCuenta.isEnabled = state !is RegistroViewModel.RegistroState.Loading

            when (state) {
                is RegistroViewModel.RegistroState.Loading -> {

                }
                is RegistroViewModel.RegistroState.Success -> {
                    Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is RegistroViewModel.RegistroState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}