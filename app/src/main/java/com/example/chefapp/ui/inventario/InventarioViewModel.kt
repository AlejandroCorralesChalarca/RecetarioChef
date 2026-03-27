package com.example.chefapp.ui.inventario

import androidx.lifecycle.ViewModel
import com.example.chefapp.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InventarioUiState(
    val productos: List<Producto> = emptyList(),
    val resumenAlertas: String = "Todos los niveles de stock están correctos.",
    val mostrarAlerta: Boolean = false,
    val isLoading: Boolean = false
)

class InventarioViewModel : ViewModel() {

    private val productosOriginales = listOf(
        Producto(1, "Aceite de Oliva", "Aceites", 45f, 20f, "L"),
        Producto(2, "Pasta Spaghetti", "Pasta", 80f, 30f, "kg"),
        Producto(3, "Queso Parmesano", "Lácteos", 15f, 10f, "kg"),
        Producto(4, "Cebolla", "Verduras", 30f, 15f, "kg"),
        Producto(5, "Ajo", "Verduras", 3f, 5f, "kg"),
        Producto(6, "Tomate", "Verduras", 50f, 20f, "kg"),
        Producto(7, "Limón", "Frutas", 2f, 8f, "kg")
    )

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private var currentQuery = ""
    private var currentCategory = "Todas"

    init {
        aplicarFiltros()
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
        var filtrados = productosOriginales

        if (currentCategory != "Todas") {
            filtrados = filtrados.filter { it.categoria == currentCategory }
        }

        if (currentQuery.isNotEmpty()) {
            filtrados = filtrados.filter { producto ->
                producto.nombre.contains(currentQuery, ignoreCase = true)
            }
        }

        val bajos = productosOriginales.filter { it.estado == "Bajo" }
        val resumen = if (bajos.isEmpty()) {
            "Todos los niveles de stock están correctos."
        } else {
            val nombres = bajos.joinToString(", ") { it.nombre }
            "${bajos.size} ingrediente(s) por debajo del nivel mínimo: $nombres"
        }

        _uiState.update { it.copy(
            productos = filtrados,
            resumenAlertas = resumen,
            mostrarAlerta = bajos.isNotEmpty()
        ) }
    }
}