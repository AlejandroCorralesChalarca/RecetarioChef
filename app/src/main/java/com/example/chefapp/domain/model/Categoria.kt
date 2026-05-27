package com.example.chefapp.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Categoria(
    @DocumentId val docId: String = "",
    val nombre: String = "",
    val tipo: String = ""
)
