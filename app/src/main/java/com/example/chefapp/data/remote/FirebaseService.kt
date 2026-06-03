package com.example.chefapp.data.remote

import android.net.Uri
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.domain.model.User
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()

    private fun getUserRoot() = auth.currentUser?.uid ?: "anonymous"

    private fun getRecetasCol() = db.collection("users").document(getUserRoot()).collection("recetas")
    private fun getProductosCol() = db.collection("users").document(getUserRoot()).collection("productos")
    private fun getPedidosCol() = db.collection("users").document(getUserRoot()).collection("pedidos")
    private fun getCategoriasCol() = db.collection("users").document(getUserRoot()).collection("categorias")
    private fun getUsersCol() = db.collection("users")

    suspend fun guardarUsuario(user: User): Boolean {
        return try {
            getUsersCol().document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al guardar perfil de usuario")
            false
        }
    }

    suspend fun getUsuario(uid: String): User? {
        return try {
            getUsersCol().document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al obtener perfil de usuario")
            null
        }
    }

    suspend fun subirImagen(uri: Uri): String? {
        val fileName = "recetas/${getUserRoot()}/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)
        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error al subir imagen")
            null
        }
    }

    suspend fun guardarReceta(receta: Receta): Boolean {
        return try {
            val docRef = if (receta.docId.isEmpty()) getRecetasCol().document() else getRecetasCol().document(receta.docId)
            docRef.set(receta.copy(docId = docRef.id)).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Fallo al guardar receta")
            false
        }
    }

    fun getRecetas(): Flow<List<Receta>> = callbackFlow {
        val subscription = getRecetasCol().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.let { trySend(it.toObjects(Receta::class.java)) }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun eliminarReceta(docId: String) = try { getRecetasCol().document(docId).delete().await(); true } catch (e: Exception) { false }

    suspend fun guardarProducto(producto: Producto): Boolean {
        return try {
            val docRef = if (producto.docId.isEmpty()) getProductosCol().document() else getProductosCol().document(producto.docId)
            docRef.set(producto.copy(docId = docRef.id)).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error producto")
            false
        }
    }

    fun getProductos(): Flow<List<Producto>> = callbackFlow {
        val subscription = getProductosCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snapshot?.let { trySend(it.toObjects(Producto::class.java)) }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun eliminarProducto(docId: String) = try { getProductosCol().document(docId).delete().await(); true } catch (e: Exception) { false }

    suspend fun getSiguienteNumeroPedido(): String {
        return try {
            val snapshot = getPedidosCol().orderBy("numeroPedido", Query.Direction.DESCENDING).limit(1).get().await()
            val lastNum = if (snapshot.isEmpty) 0 else snapshot.documents[0].getString("numeroPedido")?.toIntOrNull() ?: 0
            String.format(Locale.US, "%03d", lastNum + 1)
        } catch (e: Exception) { "001" }
    }

    suspend fun guardarPedido(pedido: Pedido): Boolean {
        return try {
            val docRef = if (pedido.docId.isEmpty()) getPedidosCol().document() else getPedidosCol().document(pedido.docId)
            val numero = if (pedido.numeroPedido.isEmpty()) getSiguienteNumeroPedido() else pedido.numeroPedido
            docRef.set(pedido.copy(docId = docRef.id, numeroPedido = numero)).await()
            true
        } catch (e: Exception) {
            CrashReporter.logError(e, "Error pedido")
            false
        }
    }

    fun getPedidos(): Flow<List<Pedido>> = callbackFlow {
        val subscription = getPedidosCol().orderBy("numeroPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                snapshot?.let { trySend(it.toObjects(Pedido::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun eliminarPedido(docId: String) = try { getPedidosCol().document(docId).delete().await(); true } catch (e: Exception) { false }

    suspend fun descontarInventarioPorPedido(pedido: Pedido) {
        try {
            for (item in pedido.items) {
                val recetaSnapshot = getRecetasCol().whereEqualTo("nombre", item.nombre).get().await()
                if (!recetaSnapshot.isEmpty) {
                    val receta = recetaSnapshot.toObjects(Receta::class.java).first()
                    for (ingredienteMap in receta.ingredientes) {
                        for ((nombreIng, cantStr) in ingredienteMap) {
                            val cantNecesaria = cantStr.toFloatOrNull() ?: 0f
                            val totalADescontar = cantNecesaria * item.cantidad
                            val prodSnapshot = getProductosCol().whereEqualTo("nombre", nombreIng).get().await()
                            if (!prodSnapshot.isEmpty) {
                                val prodDoc = prodSnapshot.documents.first()
                                val producto = prodDoc.toObject(Producto::class.java)
                                if (producto != null) {
                                    val nuevaCant = producto.cantidadActual - totalADescontar
                                    getProductosCol().document(prodDoc.id).update("cantidadActual", nuevaCant).await()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) { CrashReporter.logError(e, "Error descuento") }
    }

    suspend fun guardarCategoria(categoria: Categoria): Boolean {
        return try {
            val docRef = if (categoria.docId.isEmpty()) getCategoriasCol().document() else getCategoriasCol().document(categoria.docId)
            docRef.set(categoria.copy(docId = docRef.id)).await()
            true
        } catch (e: Exception) { false }
    }

    fun getCategorias(): Flow<List<Categoria>> = callbackFlow {
        val subscription = getCategoriasCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snapshot?.let { trySend(it.toObjects(Categoria::class.java)) }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun eliminarCategoria(docId: String) = try { getCategoriasCol().document(docId).delete().await(); true } catch (e: Exception) { false }
}
