package com.example.chefapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pregunta 1: ¿Dónde vive el estado?
 * El estado vive en el ViewModel dentro de un StateFlow.
 */
data class MainUiState(
    val isFabMenuVisible: Boolean = false,
    val isActionInProgress: Boolean = false,
    val activeDialog: DialogType? = null
)

enum class DialogType {
    NUEVA_RECETA, NUEVO_PRODUCTO, NUEVO_PEDIDO
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun toggleFabMenu() {
        _uiState.update { it.copy(isFabMenuVisible = !it.isFabMenuVisible) }
    }

    fun hideFabMenu() {
        _uiState.update { it.copy(isFabMenuVisible = false) }
    }

    fun setActionInProgress(inProgress: Boolean) {
        _uiState.update { it.copy(isActionInProgress = inProgress) }
    }

    fun showDialog(type: DialogType) {
        _uiState.update { it.copy(activeDialog = type, isFabMenuVisible = false) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }
}
