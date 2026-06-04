package com.example.chefapp.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Pedido(
    @DocumentId val docId: String = "",
    val numeroPedido: String = "",
    val mesa: String = "",
    val tiempo: String = "",
    val total: Double = 0.0,
    val estado: String = "Pendiente",
    val categoria: String = "General",
    val items: List<ItemPedido> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class ItemPedido(
    val cantidad: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val tiempo: String = "",
    val nota: String? = null
)
