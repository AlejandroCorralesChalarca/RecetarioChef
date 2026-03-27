package com.example.chefapp.ui.recetas

import androidx.lifecycle.ViewModel
import com.example.chefapp.data.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecetasUiState(
    val recetas: List<Receta> = emptyList(),
    val isLoading: Boolean = false,
    val currentQuery: String = "",
    val currentCategory: String = "Todas",
    val selectedReceta: Receta? = null
)

class RecetasViewModel : ViewModel() {

    private val recetasOriginales = listOf(
        Receta(
            1, "Pasta Carbonara", 
            "Clásica pasta italiana con salsa cremosa de huevo y panceta", 
            "20 min", "14500", "Italiana", 5,
            listOf(
                "Pasta Spaghetti" to "0.12 kg",
                "Queso Parmesano" to "0.05 kg",
                "Aceite de Oliva" to "0.02 L",
                "Sal" to "0.005 kg",
                "Pimienta" to "0.002 kg"
            ),
            listOf(
                "Hervir agua con sal para la pasta",
                "Cocinar la pasta al dente según instrucciones del paquete",
                "Mientras tanto, batir huevos con queso parmesano rallado",
                "Cocinar la panceta en una sartén hasta que esté crujiente",
                "Escurrir la pasta y mezclar rápidamente con la mezcla de huevo",
                "Añadir la panceta y mezclar bien",
                "Servir inmediatamente con más queso y pimienta negra"
            ),
            "https://images.unsplash.com/photo-1612874742237-6526221588e3?q=80&w=800&auto=format&fit=crop"
        ),
        Receta(
            2, "Risotto de Hongos", 
            "Arroz cremoso con variedad de hongos silvestres y parmesano", 
            "35 min", "16000", "Italiana", 6,
            listOf(
                "Arroz Arborio" to "0.1 kg",
                "Hongos Mixtos" to "0.15 kg",
                "Caldo de Verduras" to "0.5 L",
                "Cebolla" to "0.03 kg",
                "Mantequilla" to "0.02 kg",
                "Vino Blanco" to "0.05 L"
            ),
            listOf(
                "Sofreír la cebolla picada en mantequilla",
                "Añadir el arroz y tostar levemente",
                "Incorporar los hongos y el vino",
                "Añadir el caldo caliente poco a poco removiendo",
                "Finalizar con parmesano y servir"
            ),
            "https://images.unsplash.com/photo-1476124369491-e7addf5db371?q=80&w=800&auto=format&fit=crop"
        ),
        Receta(
            3, "Pollo al Limón",
            "Pechuga de pollo jugosa con salsa de limón y hierbas",
            "25 min", "15500", "Aves", 4,
            listOf(
                "Pechuga de Pollo" to "0.2 kg",
                "Limón" to "1 unidad",
                "Tomillo" to "0.002 kg",
                "Mantequilla" to "0.01 kg"
            ),
            listOf(
                "Sellar el pollo en una sartén caliente",
                "Añadir el zumo de limón y el tomillo",
                "Terminar la cocción al horno",
                "Servir con rodajas de limón fresco"
            ),
            "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80&w=800&auto=format&fit=crop"
        ),
        Receta(
            4, "Tiramisú Clásico",
            "Postre italiano de café, bizcochos y crema de mascarpone",
            "15 min", "7500", "Postres", 5,
            listOf(
                "Queso Mascarpone" to "0.25 kg",
                "Café Espresso" to "0.1 L",
                "Bizcochos de soletilla" to "0.1 kg",
                "Cacao en polvo" to "0.01 kg",
                "Azúcar" to "0.05 kg"
            ),
            listOf(
                "Preparar café fuerte y dejar enfriar",
                "Mezclar mascarpone con azúcar hasta cremar",
                "Sumergir bizcochos en café rápidamente",
                "Alternar capas de bizcocho y crema",
                "Espolvorear cacao y refrigerar 4 horas"
            ),
            "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?q=80&w=800&auto=format&fit=crop"
        )
    )

    private val _uiState = MutableStateFlow(RecetasUiState())
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    init {
        aplicarFiltros()
    }

    fun buscar(query: String) {
        if (_uiState.value.currentQuery == query) return
        _uiState.update { it.copy(currentQuery = query) }
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String) {
        if (_uiState.value.currentCategory == categoria) return
        _uiState.update { it.copy(currentCategory = categoria) }
        aplicarFiltros()
    }

    fun seleccionarReceta(receta: Receta?) {
        _uiState.update { it.copy(selectedReceta = receta) }
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var filtradas = recetasOriginales

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
}