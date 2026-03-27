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
    val isLoading: Boolean = false,
    val currentQuery: String = "",
    val currentEstado: String = "Todos"
)

class PedidosViewModel : ViewModel() {

    // Fuente de verdad mutable para que los cambios de estado persistan
    private var pedidosOriginales = mutableListOf(
        Pedido(
            "P004", "Mesa 3", "Hace 35 min", 45500.0, "Pendiente",
            listOf(
                ItemPedido(1, "Pasta Carbonara", 14500.0),
                ItemPedido(2, "Pollo al Limón", 31000.0, "Nota: Poco cocido")
            )
        ),
        Pedido(
            "P002", "Mesa 12", "Hace 38 min", 31500.0, "Pendiente",
            listOf(
                ItemPedido(1, "Risotto de Setas", 16000.0),
                ItemPedido(1, "Pollo al Limón", 15500.0)
            )
        ),
        Pedido(
            "P005", "Mesa 15", "Hace 43 min", 35000.0, "En Preparación",
            listOf(
                ItemPedido(2, "Ensalada César", 24000.0),
                ItemPedido(1, "Pasta Pomodoro", 11000.0)
            )
        ),
        Pedido(
            "P001", "Mesa 5", "Hace 48 min", 41000.0, "En Preparación",
            listOf(
                ItemPedido(2, "Pasta Carbonara", 29000.0),
                ItemPedido(1, "Ensalada César", 12000.0, "Nota: Sin anchoas")
            )
        ),
        Pedido(
            "P003", "Mesa 8", "Hace 58 min", 69000.0, "Listo",
            listOf(
                ItemPedido(3, "Pasta Pomodoro", 33000.0),
                ItemPedido(2, "Paella Valenciana", 36000.0)
            )
        )
    )

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    init {
        aplicarFiltros()
    }

    fun buscar(query: String) {
        _uiState.update { it.copy(currentQuery = query) }
        aplicarFiltros()
    }

    fun filtrarPorEstado(estado: String) {
        _uiState.update { it.copy(currentEstado = estado) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var filtrados = pedidosOriginales.toList()

        if (state.currentEstado != "Todos") {
            filtrados = filtrados.filter { it.estado == state.currentEstado }
        }

        if (state.currentQuery.isNotEmpty()) {
            filtrados = filtrados.filter { pedido ->
                pedido.mesa.contains(state.currentQuery, ignoreCase = true) ||
                        pedido.id.contains(state.currentQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(pedidos = filtrados) }
    }

    fun cambiarEstadoPedido(pedido: Pedido) {
        val nuevoEstado = when (pedido.estado) {
            "Pendiente" -> "En Preparación"
            "En Preparación" -> "Listo"
            "Listo" -> "Entregado"
            else -> pedido.estado
        }
        
        // Actualizamos la fuente de verdad (lista original)
        val index = pedidosOriginales.indexOfFirst { it.id == pedido.id }
        if (index != -1) {
            if (nuevoEstado == "Entregado") {
                pedidosOriginales.removeAt(index)
            } else {
                pedidosOriginales[index] = pedido.copy(estado = nuevoEstado)
            }
            // Re-aplicamos filtros para que la UI se actualice con la lista modificada
            aplicarFiltros()
        }
    }
}