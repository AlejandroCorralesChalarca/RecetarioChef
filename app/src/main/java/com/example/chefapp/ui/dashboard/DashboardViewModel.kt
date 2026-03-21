package com.example.chefapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _pedidosHoy = MutableLiveData<Int>()
    val pedidosHoy: LiveData<Int> = _pedidosHoy

    private val _enCocina = MutableLiveData<Int>()
    val enCocina: LiveData<Int> = _enCocina

    private val _porAbastecer = MutableLiveData<Int>()
    val porAbastecer: LiveData<Int> = _porAbastecer

    init {
        _pedidosHoy.value = 18
        _enCocina.value = 4
        _porAbastecer.value = 3
    }
}