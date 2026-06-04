package com.example.chefapp.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Receta(
    @DocumentId val docId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tiempo: String = "",
    val precio: String = "",
    val categoria: String = "Todas",
    val imageUrl: String? = null,
    val ingredientes: List<Map<String, String>> = emptyList(),
    val pasos: List<String> = emptyList()
)
