package com.example.chefapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isFabMenuVisible: Boolean = false,
    val isActionInProgress: Boolean = false,
    val activeDialog: DialogType? = null,
    val listaRecetas: List<Receta> = emptyList(),
    val listaProductos: List<Producto> = emptyList(),
    val listaCategorias: List<Categoria> = emptyList(),
    val recetaSeleccionada: Receta? = null,
    val recetaAEditar: Receta? = null,
    val productoAEditar: Producto? = null,
    val pedidoAEditar: Pedido? = null
)

enum class DialogType {
    NUEVA_RECETA, NUEVO_PRODUCTO, NUEVO_PEDIDO, GESTION_CATEGORIAS
}

class MainViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        cargarDatosBase()
    }

    private fun cargarDatosBase() {
        viewModelScope.launch {
            firebaseService.getRecetas().collect { recetas ->
                _uiState.update { it.copy(listaRecetas = recetas) }
            }
        }
        viewModelScope.launch {
            firebaseService.getProductos().collect { productos ->
                _uiState.update { it.copy(listaProductos = productos) }
            }
        }
        viewModelScope.launch {
            firebaseService.getCategorias().collect { categorias ->
                _uiState.update { it.copy(listaCategorias = categorias) }
            }
        }
    }

    fun toggleFabMenu() {
        _uiState.update { it.copy(isFabMenuVisible = !it.isFabMenuVisible) }
    }

    fun hideFabMenu() {
        _uiState.update { it.copy(isFabMenuVisible = false) }
    }

    fun setActionInProgress(inProgress: Boolean) {
        _uiState.update { it.copy(isActionInProgress = inProgress) }
    }

    fun showDialog(type: DialogType, item: Any? = null) {
        _uiState.update { state ->
            state.copy(
                activeDialog = type,
                isFabMenuVisible = false,
                recetaAEditar = if (item is Receta) item else null,
                productoAEditar = if (item is Producto) item else null,
                pedidoAEditar = if (item is Pedido) item else null
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null, recetaAEditar = null, productoAEditar = null, pedidoAEditar = null) }
    }

    fun setRecetaSeleccionada(receta: Receta?) {
        _uiState.update { it.copy(recetaSeleccionada = receta) }
    }

    fun guardarReceta(
        docId: String = "",
        nombre: String,
        descripcion: String,
        tiempo: String,
        precio: String,
        categoria: String,
        ingredientes: List<Map<String, String>> = emptyList(),
        pasos: List<String> = emptyList(),
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            setActionInProgress(true)
            val imageUrl = imageUri?.let { firebaseService.subirImagen(it) } ?: uiState.value.recetaAEditar?.imageUrl
            val nuevaReceta = Receta(
                docId = docId,
                nombre = nombre,
                descripcion = descripcion,
                tiempo = tiempo,
                precio = precio,
                categoria = categoria,
                imageUrl = imageUrl,
                ingredientes = ingredientes,
                pasos = pasos
            )
            firebaseService.guardarReceta(nuevaReceta)
            setActionInProgress(false)
            dismissDialog()
        }
    }

    fun eliminarReceta(docId: String) {
        viewModelScope.launch {
            firebaseService.eliminarReceta(docId)
        }
    }

    fun guardarProducto(
        docId: String = "",
        nombre: String,
        categoria: String,
        cantidad: Float,
        minimo: Float,
        unidad: String
    ) {
        viewModelScope.launch {
            setActionInProgress(true)
            val nuevoProducto = Producto(
                docId = docId,
                nombre = nombre,
                categoria = categoria,
                cantidadActual = cantidad,
                stockMinimo = minimo,
                unidad = unidad
            )
            firebaseService.guardarProducto(nuevoProducto)
            setActionInProgress(false)
            dismissDialog()
        }
    }

    fun eliminarProducto(docId: String) {
        viewModelScope.launch {
            firebaseService.eliminarProducto(docId)
        }
    }

    fun guardarPedido(pedido: Pedido) {
        viewModelScope.launch {
            setActionInProgress(true)
            firebaseService.guardarPedido(pedido)
            setActionInProgress(false)
            dismissDialog()
        }
    }

    fun eliminarPedido(docId: String) {
        viewModelScope.launch {
            firebaseService.eliminarPedido(docId)
        }
    }

    fun guardarCategoria(nombre: String, tipo: String) {
        viewModelScope.launch {
            firebaseService.guardarCategoria(Categoria(nombre = nombre, tipo = tipo))
        }
    }

    fun eliminarCategoria(docId: String) {
        viewModelScope.launch {
            firebaseService.eliminarCategoria(docId)
        }
    }
}
