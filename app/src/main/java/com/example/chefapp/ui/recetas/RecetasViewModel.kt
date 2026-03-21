package com.example.chefapp.ui.recetas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Receta(
    val id: Int,
    val emoji: String,
    val nombre: String,
    val tiempo: String,
    val porciones: String,
    val ingredientes: String
)

class RecetasViewModel : ViewModel() {

    private val recetasOriginales = listOf(
        Receta(1, "🍝", "Pasta Carbonara", "25 min", "2 porciones", "Pasta, huevos, panceta, queso parmesano, pimienta negra."),
        Receta(2, "🥩", "Filete de Res", "40 min", "1 porción", "Filete de res, ajo, romero, mantequilla, sal, pimienta."),
        Receta(3, "🍕", "Pizza Margarita", "35 min", "4 porciones", "Harina, tomate, mozzarella, albahaca, aceite de oliva, sal."),
        Receta(4, "🍮", "Tiramisú", "50 min", "6 porciones", "Queso mascarpone, huevos, azúcar, café, bizcochos, cacao.")
    )

    private val _recetasFiltradas = MutableLiveData<List<Receta>>()
    val recetasFiltradas: LiveData<List<Receta>> = _recetasFiltradas

    init {
        _recetasFiltradas.value = recetasOriginales
    }

    fun buscar(query: String) {
        _recetasFiltradas.value = if (query.isEmpty()) {
            recetasOriginales
        } else {
            recetasOriginales.filter { receta ->
                receta.nombre.contains(query, ignoreCase = true) ||
                        receta.ingredientes.contains(query, ignoreCase = true)
            }
        }
    }
}