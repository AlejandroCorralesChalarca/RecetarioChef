package com.example.chefapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.*
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isFabMenuVisible: Boolean = false,
    val isActionInProgress: Boolean = false,
    val actionStatus: UiState<String>? = null,
    val activeDialog: DialogType? = null,
    val listaRecetas: List<Receta> = emptyList(),
    val listaProductos: List<Producto> = emptyList(),
    val listaCategorias: List<Categoria> = emptyList(),
    val recetaSeleccionada: Receta? = null,
    val recetaAEditar: Receta? = null,
    val productoAEditar: Producto? = null,
    val pedidoAEditar: Pedido? = null
)

enum class DialogType { NUEVA_RECETA, NUEVO_PRODUCTO, NUEVO_PEDIDO, GESTION_CATEGORIAS }

class MainViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init { cargarDatosBase() }

    private fun cargarDatosBase() {
        viewModelScope.launch { firebaseService.getRecetas().collect { r -> _uiState.update { it.copy(listaRecetas = r) } } }
        viewModelScope.launch { firebaseService.getProductos().collect { p -> _uiState.update { it.copy(listaProductos = p) } } }
        viewModelScope.launch { firebaseService.getCategorias().collect { c -> _uiState.update { it.copy(listaCategorias = c) } } }
    }

    fun showDialog(type: DialogType, item: Any? = null) {
        _uiState.update { it.copy(activeDialog = type, isFabMenuVisible = false,
            recetaAEditar = item as? Receta, productoAEditar = item as? Producto, pedidoAEditar = item as? Pedido) }
    }

    fun dismissDialog() { _uiState.update { it.copy(activeDialog = null, actionStatus = null) } }
    fun toggleFabMenu() { _uiState.update { it.copy(isFabMenuVisible = !it.isFabMenuVisible) } }
    fun hideFabMenu() { _uiState.update { it.copy(isFabMenuVisible = false) } }

    fun guardarReceta(docId: String, nombre: String, desc: String, tiempo: String, precio: String, cat: String, ing: List<Map<String, String>>, pasos: List<String>, uri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionStatus = UiState.Loading) }
            try {
                val url = uri?.let { firebaseService.subirImagen(it) } ?: _uiState.value.recetaAEditar?.imageUrl
                val receta = Receta(docId, nombre, desc, tiempo, precio, cat, url, ing, pasos)
                firebaseService.guardarReceta(receta)
                _uiState.update { it.copy(isActionInProgress = false, actionStatus = UiState.Success("Guardado"), activeDialog = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionInProgress = false, actionStatus = UiState.Error(e.message ?: "Error")) }
            }
        }
    }

    fun guardarProducto(docId: String, nombre: String, cat: String, cant: Float, min: Float, unidad: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionStatus = UiState.Loading) }
            try {
                firebaseService.guardarProducto(Producto(docId, nombre, cat, cant, min, unidad))
                _uiState.update { it.copy(isActionInProgress = false, actionStatus = UiState.Success("Producto Guardado"), activeDialog = null) }
            } catch (e: Exception) { _uiState.update { it.copy(isActionInProgress = false) } }
        }
    }

    fun guardarPedido(pedido: Pedido) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            firebaseService.guardarPedido(pedido)
            _uiState.update { it.copy(isActionInProgress = false, activeDialog = null) }
        }
    }

    fun eliminarReceta(id: String) { viewModelScope.launch { firebaseService.eliminarReceta(id) } }
    fun eliminarProducto(id: String) { viewModelScope.launch { firebaseService.eliminarProducto(id) } }
    fun eliminarPedido(id: String) { viewModelScope.launch { firebaseService.eliminarPedido(id) } }
    fun guardarCategoria(n: String, t: String) { viewModelScope.launch { firebaseService.guardarCategoria(Categoria(nombre = n, tipo = t)) } }
    fun eliminarCategoria(id: String) { viewModelScope.launch { firebaseService.eliminarCategoria(id) } }
    fun setRecetaSeleccionada(r: Receta?) { _uiState.update { it.copy(recetaSeleccionada = r) } }
}