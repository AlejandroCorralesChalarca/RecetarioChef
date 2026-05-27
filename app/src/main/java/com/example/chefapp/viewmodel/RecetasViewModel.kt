package com.example.chefapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.domain.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecetasUiState(
    val recetas: List<Receta> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val isLoading: Boolean = false,
    val currentQuery: String = "",
    val currentCategory: String = "Todas",
    val selectedReceta: Receta? = null,
    val error: String? = null
)

class RecetasViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var todasLasRecetas = emptyList<Receta>()

    private val _uiState = MutableStateFlow(RecetasUiState())
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    init {
        cargarRecetas()
        cargarCategorias()
    }

    private fun cargarRecetas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            firebaseService.getRecetas().collect { recetas ->
                todasLasRecetas = recetas
                aplicarFiltros()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            firebaseService.getCategorias().collect { cats ->
                val filtradas = cats.filter { it.tipo == "RECETA" }
                _uiState.update { it.copy(categorias = filtradas) }
            }
        }
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
        imageUri: Uri?,
        existingImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val imageUrl = imageUri?.let { firebaseService.subirImagen(it) } ?: existingImageUrl
            
            val recetaParaGuardar = Receta(
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

            val exito = firebaseService.guardarReceta(recetaParaGuardar)
            if (!exito) {
                _uiState.update { it.copy(error = "Error al guardar la receta") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun buscar(query: String) {
        _uiState.update { it.copy(currentQuery = query) }
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String) {
        _uiState.update { it.copy(currentCategory = categoria) }
        aplicarFiltros()
    }

    fun seleccionarReceta(receta: Receta?) {
        _uiState.update { it.copy(selectedReceta = receta) }
    }

    fun eliminarReceta(receta: Receta) {
        viewModelScope.launch {
            val exito = firebaseService.eliminarReceta(receta.docId)
            if (!exito) {
                _uiState.update { it.copy(error = "Error al eliminar la receta") }
            }
        }
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var filtradas = todasLasRecetas

        if (state.currentCategory != "Todas") {
            filtradas = filtradas.filter { it.categoria == state.currentCategory }
        }

        if (state.currentQuery.isNotEmpty()) {
            filtradas = filtradas.filter { receta ->
                receta.nombre.contains(state.currentQuery, ignoreCase = true) ||
                        receta.descripcion.contains(state.currentQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(recetas = filtradas) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
