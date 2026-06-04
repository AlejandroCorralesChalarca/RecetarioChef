package com.example.chefapp.data.repository

import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.*
import kotlinx.coroutines.flow.Flow

class ChefRepository {
    private val firebaseService = FirebaseService()

    fun getRecetas(): Flow<List<Receta>> = firebaseService.getRecetas()
    suspend fun guardarReceta(receta: Receta) = firebaseService.guardarReceta(receta)
    suspend fun eliminarReceta(docId: String) = firebaseService.eliminarReceta(docId)

    fun getPedidos(): Flow<List<Pedido>> = firebaseService.getPedidos()
    suspend fun guardarPedido(pedido: Pedido) = firebaseService.guardarPedido(pedido)
    suspend fun cambiarEstadoPedido(pedido: Pedido) = firebaseService.guardarPedido(pedido)

    fun getProductos(): Flow<List<Producto>> = firebaseService.getProductos()
    suspend fun guardarProducto(producto: Producto) = firebaseService.guardarProducto(producto)
    suspend fun eliminarProducto(docId: String) = firebaseService.eliminarProducto(docId)
}
