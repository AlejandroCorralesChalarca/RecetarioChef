package com.example.chefapp.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Producto(
    @DocumentId val docId: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val cantidadActual: Float = 0f,
    val stockMinimo: Float = 0f,
    val unidad: String = ""
) {
    @get:Exclude
    val estado: String
        get() = when {
            cantidadActual <= stockMinimo -> "Bajo"
            cantidadActual <= stockMinimo * 1.5f -> "Medio"
            else -> "Óptimo"
        }
}
