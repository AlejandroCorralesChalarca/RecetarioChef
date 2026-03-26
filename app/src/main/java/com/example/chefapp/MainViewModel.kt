package com.example.chefapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val isFabMenuVisible: Boolean = false,
    val isActionInProgress: Boolean = false
)

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
}
