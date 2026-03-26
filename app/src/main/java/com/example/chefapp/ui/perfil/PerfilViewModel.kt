package com.example.chefapp.ui.perfil

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class PerfilUiState(
    val nombre: String = "",
    val rol: String = "",
    val email: String = "",
    val isLoading: Boolean = false
)

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = PerfilUiState(
            nombre = "Chef Natalia",
            rol = "Chef Ejecutivo",
            email = "natalia@chefapp.com"
        )
    }
}
