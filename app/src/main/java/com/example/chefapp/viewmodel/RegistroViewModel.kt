package com.example.chefapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.auth.FirebaseAuth

class RegistroViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _registroState = MutableLiveData<RegistroState>()
    val registroState: LiveData<RegistroState> = _registroState

    fun registrar(nombre: String, email: String, pass: String) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _registroState.value = RegistroState.Error("Todos los campos son obligatorios")
            return
        }
        
        if (pass.length < 6) {
            _registroState.value = RegistroState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        _registroState.value = RegistroState.Loading

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registroState.value = RegistroState.Success
                } else {
                    val exception = task.exception
                    exception?.let { CrashReporter.logError(it, "Fallo al registrar usuario: $email") }
                    _registroState.value = RegistroState.Error(exception?.message ?: "Error al registrar usuario")
                }
            }
    }

    sealed class RegistroState {
        object Loading : RegistroState()
        object Success : RegistroState()
        data class Error(val message: String) : RegistroState()
    }
}
