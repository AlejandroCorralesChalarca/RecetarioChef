package com.example.chefapp.data.model

data class Pedido(
    val id: String,
    val mesa: String,
    val tiempo: String,
    val total: Double,
    val estado: String,
    val items: List<ItemPedido>
)

data class ItemPedido(
    val cantidad: Int,
    val nombre: String,
    val precio: Double,
    val nota: String? = null
)