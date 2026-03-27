package com.example.chefapp.ui.pedidos

import androidx.lifecycle.ViewModel
import com.example.chefapp.data.model.ItemPedido
import com.example.chefapp.data.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PedidosUiState(
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = false
)

class PedidosViewModel : ViewModel() {

    private val pedidosOriginales = listOf(
        Pedido(
            "P004", "Mesa 3", "Hace 35 min", 45.50, "Pendiente",
            listOf(
                ItemPedido(1, "Pasta Carbonara", 14.50),
                ItemPedido(2, "Pollo al Limón", 31.00, "Nota: Poco cocido")
            )
        ),
        Pedido(
            "P002", "Mesa 12", "Hace 38 min", 31.50, "Pendiente",
            listOf(
                ItemPedido(1, "Risotto de Setas", 16.00),
                ItemPedido(1, "Pollo al Limón", 15.50)
            )
        ),
        Pedido(
            "P005", "Mesa 15", "Hace 43 min", 35.00, "En Preparación",
            listOf(
                ItemPedido(2, "Ensalada César", 24.00),
                ItemPedido(1, "Pasta Pomodoro", 11.00)
            )
        ),
        Pedido(
            "P001", "Mesa 5", "Hace 48 min", 41.00, "En Preparación",
            listOf(
                ItemPedido(2, "Pasta Carbonara", 29.00),
                ItemPedido(1, "Ensalada César", 12.00, "Nota: Sin anchoas")
            )
        ),
        Pedido(
            "P003", "Mesa 8", "Hace 58 min", 69.00, "Listo",
            listOf(
                ItemPedido(3, "Pasta Pomodoro", 33.00),
                ItemPedido(2, "Paella Valenciana", 36.00)
            )
        )
    )

    private val _uiState = MutableStateFlow(PedidosUiState(pedidos = pedidosOriginales))
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    private var currentQuery = ""
    private var currentEstado = "Todos"

    fun buscar(query: String) {
        currentQuery = query
        aplicarFiltros()
    }

    fun filtrarPorEstado(estado: String) {
        currentEstado = estado
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var filtrados = pedidosOriginales

        if (currentEstado != "Todos") {
            filtrados = filtrados.filter { it.estado == currentEstado }
        }

        if (currentQuery.isNotEmpty()) {
            filtrados = filtrados.filter { pedido ->
                pedido.mesa.contains(currentQuery, ignoreCase = true) ||
                        pedido.id.contains(currentQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(pedidos = filtrados) }
    }

    fun cambiarEstadoPedido(pedido: Pedido) {
        _uiState.update { it.copy(isLoading = true) }
        
        val nuevoEstado = when (pedido.estado) {
            "Pendiente" -> "En Preparación"
            "En Preparación" -> "Listo"
            "Listo" -> "Entregado"
            else -> pedido.estado
        }
        
        val listaActualizada = _uiState.value.pedidos.toMutableList()
        val index = listaActualizada.indexOfFirst { it.id == pedido.id }
        if (index != -1) {
            if (nuevoEstado == "Entregado") {
                listaActualizada.removeAt(index)
            } else {
                listaActualizada[index] = pedido.copy(estado = nuevoEstado)
            }
            _uiState.update { it.copy(pedidos = listaActualizada, isLoading = false) }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}