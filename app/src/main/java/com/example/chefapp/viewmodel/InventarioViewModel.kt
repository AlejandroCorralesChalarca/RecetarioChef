package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventarioUiState(
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
    
    private val _allProductos = MutableStateFlow<List<Producto>>(emptyList())
    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    private val _currentCategory = MutableStateFlow("Todas")
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<InventarioUiState> = combine(
        _allProductos, _categorias, _currentCategory, _searchQuery, _isLoading
    ) { productos, categorias, category, query, loading ->
        val filtered = productos.filter { prod ->
            (category == "Todas" || prod.categoria == category) &&
            (query.isEmpty() || prod.nombre.contains(query, ignoreCase = true))
        }
        
        val stockBajo = productos.filter { it.cantidadActual <= it.stockMinimo }
        val resumen = if (stockBajo.isNotEmpty()) {
            "Hay ${stockBajo.size} productos con stock bajo o crítico"
        } else {
            "El inventario está en niveles óptimos"
        }

        InventarioUiState(
            productos = filtered,
            categorias = categorias.filter { it.tipo.uppercase() == "INVENTARIO" },
            currentCategory = category,
            searchQuery = query,
            isLoading = loading,
            mostrarAlerta = stockBajo.isNotEmpty(),
            resumenAlertas = resumen
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventarioUiState())

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseService.getProductos().collect {
                _allProductos.value = it
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            firebaseService.getCategorias().collect {
                _categorias.value = it
            }
        }
    }

    fun buscar(query: String) {
        _searchQuery.value = query
    }

    fun filtrarPorCategoria(categoria: String) {
        _currentCategory.value = categoria
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            firebaseService.eliminarProducto(producto.docId)
        }
    }
}
