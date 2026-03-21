package com.example.chefapp.ui.inventario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventarioViewModel : ViewModel() {

    private val _busqueda = MutableLiveData<String>()
    val busqueda: LiveData<String> = _busqueda

    init {
        _busqueda.value = ""
    }
}