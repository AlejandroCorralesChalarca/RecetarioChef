package com.example.chefapp.data.repository

import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que coordina el acceso a datos remotos (Firebase) 
 * cumpliendo con la arquitectura nativa solicitada.
 */
class ChefRepository {
    private val firebaseService = FirebaseService()

    // Recetas
    fun getRecetas(): Flow<List<Receta>> = firebaseService.getRecetas()
    suspend fun guardarReceta(receta: Receta) = firebaseService.guardarReceta(receta)
    suspend fun eliminarReceta(docId: String) = firebaseService.eliminarReceta(docId)

    // Pedidos
    fun getPedidos(): Flow<List<Pedido>> = firebaseService.getPedidos()
    suspend fun guardarPedido(pedido: Pedido) = firebaseService.guardarPedido(pedido)
    suspend fun eliminarPedido(docId: String) = firebaseService.eliminarPedido(docId)
    suspend fun cambiarEstadoPedido(pedido: Pedido) {
        // Aquí iría la lógica de coordinación si hubiera datos locales
        firebaseService.guardarPedido(pedido)
    }

    // Productos
    fun getProductos(): Flow<List<Producto>> = firebaseService.getProductos()
    suspend fun guardarProducto(producto: Producto) = firebaseService.guardarProducto(producto)

    // Categorías
    fun getCategorias(): Flow<List<Categoria>> = firebaseService.getCategorias()
    suspend fun guardarCategoria(categoria: Categoria) = firebaseService.guardarCategoria(categoria)
}
