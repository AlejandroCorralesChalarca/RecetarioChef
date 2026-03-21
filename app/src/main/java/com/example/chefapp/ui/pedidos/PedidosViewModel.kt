package com.example.chefapp.ui.pedidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Pedido(
    val id: Int,
    val mesa: String,
    val items: List<String>,
    val tiempo: String,
    var estado: String
)

class PedidosViewModel : ViewModel() {

    private val _pedidos = MutableLiveData<List<Pedido>>()
    val pedidos: LiveData<List<Pedido>> = _pedidos

    init {
        _pedidos.value = listOf(
            Pedido(1, "Mesa 4 — Pedido #001", listOf("Pasta carbonara x1", "Ensalada césar x2"), "Hace 12 minutos", "En cocina"),
            Pedido(2, "Mesa 7 — Pedido #002", listOf("Filete de res x1", "Sopa del día x1"), "Hace 25 minutos", "Listo"),
            Pedido(3, "Mesa 2 — Pedido #003", listOf("Pizza margarita x2", "Tiramisú x2"), "Hace 5 minutos", "En cocina")
        )
    }

    fun marcarComoListo(pedidoId: Int) {
        val lista = _pedidos.value?.toMutableList() ?: return
        val index = lista.indexOfFirst { it.id == pedidoId }
        if (index != -1) {
            lista[index] = lista[index].copy(estado = "Listo")
            _pedidos.value = lista
        }
    }
}