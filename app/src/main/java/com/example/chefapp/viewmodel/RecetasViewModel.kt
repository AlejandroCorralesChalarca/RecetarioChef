package com.example.chefapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecetasViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    private var todasLasRecetas = emptyList<Receta>()

    // Estado principal para la lista (Punto 7)
    private val _uiState = MutableStateFlow<UiState<List<Receta>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Receta>>> = _uiState.asStateFlow()

    // Estados secundarios que ya tenías
    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias = _categorias.asStateFlow()

    private val _selectedReceta = MutableStateFlow<Receta?>(null)
    val selectedReceta = _selectedReceta.asStateFlow()

    private var currentQuery = ""
    private var currentCategory = "Todas"

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        if (auth.currentUser == null) {
            _uiState.value = UiState.SessionExpired
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Cargar Categorías
                firebaseService.getCategorias().collect { cats ->
                    _categorias.value = cats.filter { it.tipo == "RECETA" }
                }
                
                // Cargar Recetas
                firebaseService.getRecetas().collect { recetas ->
                    todasLasRecetas = recetas
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                manejarError(e)
            }
        }
    }

    fun guardarReceta(
        docId: String = "", nombre: String, descripcion: String, tiempo: String,
        precio: String, categoria: String, ingredientes: List<Map<String, String>>,
        pasos: List<String>, imageUri: Uri?, existingImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val imageUrl = imageUri?.let { firebaseService.subirImagen(it) } ?: existingImageUrl
                val receta = Receta(docId, nombre, descripcion, tiempo, precio, categoria, imageUrl, ingredientes, pasos)
                
                val exito = firebaseService.guardarReceta(receta)
                if (exito) {
                    cargarDatos() // Recargar para confirmar éxito
                } else {
                    _uiState.value = UiState.Error("No se pudo guardar la receta en la base de datos")
                }
            } catch (e: Exception) {
                manejarError(e)
            }
        }
    }

    fun eliminarReceta(receta: Receta) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val exito = firebaseService.eliminarReceta(receta.docId)
                if (!exito) _uiState.value = UiState.Error("Error al eliminar")
                // El collect de getRecetas en init se encargará de actualizar la lista
            } catch (e: Exception) {
                manejarError(e)
            }
        }
    }

    fun buscar(query: String) {
        currentQuery = query
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String) {
        currentCategory = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var filtradas = todasLasRecetas
        if (currentCategory != "Todas") filtradas = filtradas.filter { it.categoria == currentCategory }
        if (currentQuery.isNotEmpty()) filtradas = filtradas.filter { 
            it.nombre.contains(currentQuery, ignoreCase = true) || it.descripcion.contains(currentQuery, ignoreCase = true)
        }

        _uiState.value = if (filtradas.isEmpty()) UiState.Empty else UiState.Success(filtradas)
    }

    fun seleccionarReceta(receta: Receta?) { _selectedReceta.value = receta }

    private fun manejarError(e: Exception) {
        CrashReporter.logError(e, "Error en RecetasViewModel")
        _uiState.value = when (e) {
            is FirebaseNetworkException -> UiState.NoConnection
            else -> UiState.Error(e.message ?: "Error inesperado")
        }
    }
}