package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val rol: String = "Chef",
    val isLoggedOut: Boolean = false
)

class PerfilViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val user = authManager.currentUser
        _uiState.update { it.copy(
            nombre = user?.displayName ?: "Usuario Chef",
            email = user?.email ?: "chef@recetario.com"
        ) }
    }

    fun cerrarSesion() {
        authManager.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
