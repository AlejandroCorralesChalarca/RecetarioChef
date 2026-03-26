package com.example.chefapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistroViewModel : ViewModel() {

    private val _registroState = MutableLiveData<RegistroState>()
    val registroState: LiveData<RegistroState> = _registroState

    fun registrar(nombre: String, email: String, pass: String) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _registroState.value = RegistroState.Error("Todos los campos son obligatorios")
        } else {
            _registroState.value = RegistroState.Loading

            _registroState.value = RegistroState.Success
        }
    }

    sealed class RegistroState {
        object Loading : RegistroState()
        object Success : RegistroState()
        data class Error(val message: String) : RegistroState()
    }
}