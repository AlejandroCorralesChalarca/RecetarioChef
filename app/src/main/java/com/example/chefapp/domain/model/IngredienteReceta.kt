package com.example.chefapp.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class IngredienteReceta(
    val nombre: String = "",
    val cantidad: String = "",
    val productoDocId: String? = null
)
