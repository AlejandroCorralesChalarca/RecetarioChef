package com.example.chefapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chefapp.databinding.ActivityLoginBinding
import com.example.chefapp.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val pass = binding.editPassword.text.toString()
            viewModel.onLoginClicked(email, pass)
        }

        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.txtOlvidoPassword.setOnClickListener {
            val email = binding.editEmail.text.toString()
            viewModel.onResetPasswordClicked(email)
        }
    }

    private fun setupObservers() {
    viewModel.loginState.observe(this) { state ->
        // Bloqueamos el botón si está cargando
        binding.btnLogin.isEnabled = state !is LoginViewModel.LoginState.Loading
        binding.progressBar.visibility = if (state is LoginViewModel.LoginState.Loading) View.VISIBLE else View.GONE

        when (state) {
            is LoginViewModel.LoginState.Success -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is LoginViewModel.LoginState.NoConnection -> {
                Toast.makeText(this, "Error de red. Verifica tu conexión.", Toast.LENGTH_LONG).show()
            }
            is LoginViewModel.LoginState.Error -> {
                // Aquí se cumple el requisito de mostrar error de credenciales
                binding.editPassword.error = "Credenciales incorrectas"
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    }
}
