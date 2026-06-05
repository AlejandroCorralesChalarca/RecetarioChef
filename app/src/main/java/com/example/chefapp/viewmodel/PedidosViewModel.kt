package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PedidosUiState(
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val noConnection: Boolean = false,
    val error: String? = null
    val categorias: List<Categoria> = emptyList(),
    val currentQuery: String = "",
    val currentEstado: String = "Todos",
    val currentCategory: String = "Todas",
    val selectedPedido: Pedido? = null
)

class PedidosViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var pedidosOriginales = emptyList<Pedido>()

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
        cargarCategorias()
    }

    private fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, noConnection = false) }
            
            firebaseService.getPedidos().collect { pedidos ->
                if (pedidos.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, isEmpty = true, pedidos = emptyList()) }
                } else {
                    pedidosOriginales = pedidos
                    aplicarFiltros() // Esta función debe poner isLoading = false e isEmpty = false
                }
            }
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            firebaseService.getCategorias().collect { cats ->
                val filtradas = cats.filter { it.tipo == "RECETA" }
                _uiState.update { it.copy(categorias = filtradas) }
            }
        }
    }

    fun buscar(query: String) {
        _uiState.update { it.copy(currentQuery = query) }
        aplicarFiltros()
    }

    fun filtrarPorEstado(estado: String) {
        _uiState.update { it.copy(currentEstado = estado) }
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String) {
        _uiState.update { it.copy(currentCategory = categoria) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var filtrados = pedidosOriginales

        if (state.currentEstado != "Todos") {
            filtrados = filtrados.filter { it.estado == state.currentEstado }
        }

        if (state.currentCategory != "Todas") {
            filtrados = filtrados.filter { it.categoria == state.currentCategory }
        }

        if (state.currentQuery.isNotEmpty()) {
            filtrados = filtrados.filter { pedido ->
                pedido.mesa.contains(state.currentQuery, ignoreCase = true) ||
                        pedido.docId.contains(state.currentQuery, ignoreCase = true) ||
                        pedido.numeroPedido.contains(state.currentQuery)
            }
        }

        _uiState.update { it.copy(pedidos = filtrados) }
    }

    fun seleccionarPedido(pedido: Pedido?) {
        _uiState.update { it.copy(selectedPedido = pedido) }
    }

    fun eliminarPedido(pedidoId: String) {
        viewModelScope.launch {
            firebaseService.eliminarPedido(pedidoId)
        }
    }

    fun cambiarEstadoPedido(pedido: Pedido) {
        val nuevoEstado = when (pedido.estado) {
            "Pendiente" -> "En Preparación"
            "En Preparación" -> "Listo"
            "Listo" -> "Finalizado"
            else -> pedido.estado
        }
        
        viewModelScope.launch {
            if (pedido.estado == "En Preparación" && nuevoEstado == "Listo") {
                firebaseService.descontarInventarioPorPedido(pedido)
            }
            firebaseService.guardarPedido(pedido.copy(estado = nuevoEstado))
        }
    }
}
