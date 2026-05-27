package com.example.chefapp.data.remote

import android.net.Uri
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseService {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val recetasCollection = db.collection("recetas")
    private val productosCollection = db.collection("productos")
    private val pedidosCollection = db.collection("pedidos")
    private val categoriasCollection = db.collection("categorias")

    suspend fun subirImagen(uri: Uri): String? {
        val fileName = "recetas/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)
        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al subir imagen a Storage")
            null
        }
    }

    suspend fun guardarReceta(receta: Receta): Boolean {
        return try {
            val docRef = if (receta.docId.isEmpty()) {
                recetasCollection.document()
            } else {
                recetasCollection.document(receta.docId)
            }
            val recetaConId = receta.copy(docId = docRef.id)
            docRef.set(recetaConId).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al guardar receta: ${receta.nombre}")
            false
        }
    }

    suspend fun eliminarReceta(docId: String): Boolean {
        return try {
            recetasCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al eliminar receta ID: $docId")
            false
        }
    }

    fun getRecetas(): Flow<List<Receta>> = callbackFlow {
        val subscription = recetasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                CrashReporter.logError(error, "Error en Snapshot de Recetas")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val recetas = snapshot.toObjects(Receta::class.java)
                trySend(recetas)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun guardarProducto(producto: Producto): Boolean {
        return try {
            val docRef = if (producto.docId.isEmpty()) {
                productosCollection.document()
            } else {
                productosCollection.document(producto.docId)
            }
            val productoConId = producto.copy(docId = docRef.id)
            docRef.set(productoConId).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al guardar producto: ${producto.nombre}")
            false
        }
    }

    suspend fun eliminarProducto(docId: String): Boolean {
        return try {
            productosCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al eliminar producto ID: $docId")
            false
        }
    }

    fun getProductos(): Flow<List<Producto>> = callbackFlow {
        val subscription = productosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                CrashReporter.logError(error, "Error en Snapshot de Productos")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val productos = snapshot.toObjects(Producto::class.java)
                trySend(productos)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getSiguienteNumeroPedido(): String {
        return try {
            val snapshot = pedidosCollection.orderBy("numeroPedido", Query.Direction.DESCENDING).limit(1).get().await()
            val lastNum = if (snapshot.isEmpty) 0 else snapshot.documents[0].getString("numeroPedido")?.toIntOrNull() ?: 0
            String.format(Locale.US, "%03d", lastNum + 1)
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al obtener siguiente número de pedido")
            "001"
        }
    }

    suspend fun guardarPedido(pedido: Pedido): Boolean {
        return try {
            val docRef = if (pedido.docId.isEmpty()) {
                pedidosCollection.document()
            } else {
                pedidosCollection.document(pedido.docId)
            }
            val pedidoConId = if (pedido.numeroPedido.isEmpty()) {
                pedido.copy(docId = docRef.id, numeroPedido = getSiguienteNumeroPedido())
            } else {
                pedido.copy(docId = docRef.id)
            }
            docRef.set(pedidoConId).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al guardar pedido")
            false
        }
    }

    suspend fun eliminarPedido(docId: String): Boolean {
        return try {
            pedidosCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al eliminar pedido ID: $docId")
            false
        }
    }

    fun getPedidos(): Flow<List<Pedido>> = callbackFlow {
        val subscription = pedidosCollection.orderBy("numeroPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    CrashReporter.logError(error, "Error en Snapshot de Pedidos")
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pedidos = snapshot.toObjects(Pedido::class.java)
                    trySend(pedidos)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun descontarInventarioPorPedido(pedido: Pedido) {
        try {
            for (item in pedido.items) {
                val recetaSnapshot = recetasCollection.whereEqualTo("nombre", item.nombre).get().await()
                if (!recetaSnapshot.isEmpty) {
                    val receta = recetaSnapshot.toObjects(Receta::class.java).first()
                    for (ingredienteMap in receta.ingredientes) {
                        for ((nombreIng, cantStr) in ingredienteMap) {
                            val cantNecesaria = cantStr.toFloatOrNull() ?: 0f
                            val totalADescontar = cantNecesaria * item.cantidad
                            
                            val prodSnapshot = productosCollection.whereEqualTo("nombre", nombreIng).get().await()
                            if (!prodSnapshot.isEmpty) {
                                val prodDoc = prodSnapshot.documents.first()
                                val producto = prodDoc.toObject(Producto::class.java)
                                if (producto != null) {
                                    val nuevaCant = producto.cantidadActual - totalADescontar
                                    productosCollection.document(prodDoc.id)
                                        .update("cantidadActual", nuevaCant).await()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al descontar inventario por pedido")
        }
    }

    suspend fun guardarCategoria(categoria: Categoria): Boolean {
        return try {
            val docRef = if (categoria.docId.isEmpty()) {
                categoriasCollection.document()
            } else {
                categoriasCollection.document(categoria.docId)
            }
            val catConId = categoria.copy(docId = docRef.id)
            docRef.set(catConId).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al guardar categoría: ${categoria.nombre}")
            false
        }
    }

    fun getCategorias(): Flow<List<Categoria>> = callbackFlow {
        val subscription = categoriasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                CrashReporter.logError(error, "Error en Snapshot de Categorías")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cats = snapshot.toObjects(Categoria::class.java)
                trySend(cats)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun eliminarCategoria(docId: String): Boolean {
        return try {
            categoriasCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al eliminar categoría ID: $docId")
            false
        }
    }
}
