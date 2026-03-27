package com.example.chefapp.ui.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val totalPedidos: Int = 0,
    val completados: Int = 0,
    val enProceso: Int = 0,
    val ingresos: String = "0.00",
    val isLoading: Boolean = false
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = DashboardUiState(
            totalPedidos = 23,
            completados = 18,
            enProceso = 5,
            ingresos = "1.245.500"
        )
    }
}