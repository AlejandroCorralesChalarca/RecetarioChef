package com.example.chefapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.crash.CrashReporter
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseService = FirebaseService()
    private val _registroState = MutableLiveData<RegistroState>()
    val registroState: LiveData<RegistroState> = _registroState

    fun registrar(nombre: String, email: String, telefono: String, restaurante: String, direccion: String, pass: String) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || telefono.isEmpty() || restaurante.isEmpty() || direccion.isEmpty()) {
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
                    val uid = task.result?.user?.uid ?: ""
                    val newUser = User(
                        uid = uid,
                        nombre = nombre,
                        email = email,
                        telefono = telefono,
                        restaurante = restaurante,
                        direccion = direccion
                    )
                    
                    viewModelScope.launch {
                        val guardado = firebaseService.guardarUsuario(newUser)
                        if (guardado) {
                            _registroState.value = RegistroState.Success
                        } else {
                            _registroState.value = RegistroState.Error("Usuario creado pero no se pudo guardar el perfil")
                        }
                    }
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
