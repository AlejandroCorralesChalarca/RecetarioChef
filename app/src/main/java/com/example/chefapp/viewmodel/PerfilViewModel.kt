package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.auth.AuthManager
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val info: UiState<com.example.chefapp.domain.model.User> = UiState.Loading,
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val restaurante: String = "",
    val direccion: String = "",
    val rol: String = "Chef",
    val isLoggedOut: Boolean = false
)

class PerfilViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    fun cargarDatosUsuario() {
        val currentUser = authManager.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(info = UiState.SessionExpired) }
            return
        }

        _uiState.update { it.copy(info = UiState.Loading) }

        viewModelScope.launch {
            try {
                val userProfile = firebaseService.getUsuario(currentUser.uid)
                if (userProfile != null) {
                    _uiState.update { it.copy(
                        info = UiState.Success(userProfile),
                        nombre = userProfile.nombre,
                        email = userProfile.email,
                        telefono = userProfile.telefono,
                        restaurante = userProfile.restaurante,
                        direccion = userProfile.direccion,
                        rol = "Chef"
                    ) }
                } else {
                    // Fallback a Auth si no hay Firestore
                    _uiState.update { it.copy(
                        info = UiState.Empty,
                        nombre = currentUser.displayName ?: "Usuario Chef",
                        email = currentUser.email ?: ""
                    ) }
                }
            } catch (e: Exception) {
                CrashReporter.logError(e, "Fallo cargando perfil")
                if (e is FirebaseNetworkException) {
                    _uiState.update { it.copy(info = UiState.NoConnection) }
                } else {
                    _uiState.update { it.copy(info = UiState.Error(e.message ?: "Error desconocido")) }
                }
            }
        }
    }

    fun cerrarSesion() {
        authManager.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}