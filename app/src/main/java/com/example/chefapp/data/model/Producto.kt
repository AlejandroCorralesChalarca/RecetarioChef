package com.example.chefapp.data.model

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val cantidadActual: Float,
    val stockMinimo: Float,
    val unidad: String
) {
    val estado: String
        get() = when {
            cantidadActual <= stockMinimo -> "Bajo"
            cantidadActual <= stockMinimo * 1.5f -> "Medio"
            else -> "Óptimo"
        }
}