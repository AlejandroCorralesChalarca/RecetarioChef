package com.example.chefapp.data.model

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tiempo: String,
    val precio: String,
    val categoria: String,
    val ingredientesCount: Int,
    val ingredientes: List<Pair<String, String>>,
    val pasos: List<String>,
    val imageUrl: String? = null
)