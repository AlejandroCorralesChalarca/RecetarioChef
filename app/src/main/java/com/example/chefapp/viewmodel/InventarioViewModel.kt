package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventarioUiStateFull(
    val mainState: UiState<List<Producto>> = UiState.Loading,
    val productos: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val currentCategory: String = "Todas",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val mostrarAlerta: Boolean = false,
    val resumenAlertas: String = ""
)

class InventarioViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    
    private val _allProductos = MutableStateFlow<List<Producto>>(emptyList())
    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    private val _currentCategory = MutableStateFlow("Todas")
    private val _searchQuery = MutableStateFlow("")
    private val _errorState = MutableStateFlow<String?>(null)
    private val _isNetworkError = MutableStateFlow(false)

    val uiState: StateFlow<InventarioUiStateFull> = combine(
        _allProductos, _categorias, _currentCategory, _searchQuery, _errorState, _isNetworkError
    ) { productos, categorias, category, query, error, networkError ->
        
        val filtered = productos.filter { prod ->
            (category == "Todas" || prod.categoria == category) &&
            (query.isEmpty() || prod.nombre.contains(query, ignoreCase = true))
        }
        
        val stockBajo = productos.filter { it.cantidadActual <= it.stockMinimo }
        
        val state = when {
            auth.currentUser == null -> UiState.SessionExpired
            networkError -> UiState.NoConnection
            error != null -> UiState.Error(error)
            productos.isEmpty() && !_isNetworkError.value -> UiState.Empty
            else -> UiState.Success(filtered)
        }

        InventarioUiStateFull(
            mainState = state,
            productos = filtered,
            categorias = categorias.filter { it.tipo.uppercase() == "INVENTARIO" },
            currentCategory = category,
            searchQuery = query,
            isLoading = state is UiState.Loading,
            mostrarAlerta = stockBajo.isNotEmpty(),
            resumenAlertas = if (stockBajo.isNotEmpty()) "Hay ${stockBajo.size} productos con stock bajo o crítico" else "El inventario está en niveles óptimos"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventarioUiStateFull())

    init { cargarDatos() }

    fun cargarDatos() {
        _isNetworkError.value = false
        viewModelScope.launch {
            try {
                firebaseService.getProductos().collect { _allProductos.value = it }
            } catch (e: Exception) {
                manejarError(e)
            }
        }
        viewModelScope.launch {
            try {
                firebaseService.getCategorias().collect { _categorias.value = it }
            } catch (e: Exception) { /* Silencioso para categorías */ }
        }
    }

    private fun manejarError(e: Exception) {
        CrashReporter.logError(e, "Error Inventario")
        if (e is FirebaseNetworkException) {
            _isNetworkError.value = true
        } else {
            _errorState.value = e.message ?: "Error desconocido"
        }
    }

    fun buscar(query: String) { _searchQuery.value = query }
    fun filtrarPorCategoria(categoria: String) { _currentCategory.value = categoria }
    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                firebaseService.eliminarProducto(producto.docId)
            } catch (e: Exception) { manejarError(e) }
        }
    }
}