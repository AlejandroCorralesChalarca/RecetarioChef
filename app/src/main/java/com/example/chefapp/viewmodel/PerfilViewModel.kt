package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.auth.AuthManager
import com.example.chefapp.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val restaurante: String = "",
    val direccion: String = "",
    val rol: String = "Chef",
    val isLoggedOut: Boolean = false,
    val isLoading: Boolean = false
)

class PerfilViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val currentUser = authManager.currentUser
        val uid = currentUser?.uid ?: return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val userProfile = firebaseService.getUsuario(uid)
            if (userProfile != null) {
                _uiState.update { it.copy(
                    nombre = userProfile.nombre,
                    email = userProfile.email,
                    telefono = userProfile.telefono,
                    restaurante = userProfile.restaurante,
                    direccion = userProfile.direccion,
                    isLoading = false
                ) }
            } else {
                // Fallback a los datos básicos de Auth si no hay perfil en Firestore
                _uiState.update { it.copy(
                    nombre = currentUser.displayName ?: "Usuario Chef",
                    email = currentUser.email ?: "",
                    isLoading = false
                ) }
            }
        }
    }

    fun cerrarSesion() {
        authManager.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
