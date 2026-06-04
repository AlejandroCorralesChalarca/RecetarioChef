package com.example.chefapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecetasViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    private var todasLasRecetas = emptyList<Receta>()

    // El estado principal ahora es un UiState que envuelve la lista
    private val _uiState = MutableStateFlow<UiState<List<Receta>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Receta>>> = _uiState.asStateFlow()

    // Estados adicionales para filtros y categorías
    private val _categorias = MutableStateFlow<List<com.example.chefapp.domain.model.Categoria>>(emptyList())
    val categorias = _categorias.asStateFlow()

    init { cargarDatos() }

    fun cargarDatos() {
        if (auth.currentUser == null) {
            _uiState.value = UiState.SessionExpired
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Cargar categorías primero
                firebaseService.getCategorias().collect { _categorias.value = it.filter { c -> c.tipo == "RECETA" } }
                
                // Cargar recetas
                firebaseService.getRecetas().collect { recetas ->
                    todasLasRecetas = recetas
                    actualizarEstadoSegunFiltros("", "Todas")
                }
            } catch (e: Exception) {
                manejarError(e)
            }
        }
    }

    private fun manejarError(e: Exception) {
        CrashReporter.logError(e, "Error en RecetasViewModel")
        when (e) {
            is FirebaseNetworkException -> _uiState.value = UiState.NoConnection
            else -> _uiState.value = UiState.Error(e.message ?: "Error desconocido")
        }
    }

    fun aplicarFiltros(query: String, categoria: String) {
        actualizarEstadoSegunFiltros(query, categoria)
    }

    private fun actualizarEstadoSegunFiltros(query: String, categoria: String) {
        var filtradas = todasLasRecetas
        if (categoria != "Todas") filtradas = filtradas.filter { it.categoria == categoria }
        if (query.isNotEmpty()) filtradas = filtradas.filter { it.nombre.contains(query, ignoreCase = true) }

        _uiState.value = if (filtradas.isEmpty()) UiState.Empty else UiState.Success(filtradas)
    }
}