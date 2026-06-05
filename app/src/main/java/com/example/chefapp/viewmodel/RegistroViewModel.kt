package com.example.chefapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.crash.CrashReporter
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.User
import com.example.chefapp.ui.UiState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseService = FirebaseService()
    
    private val _registroState = MutableLiveData<UiState<Boolean>>()
    val registroState: LiveData<UiState<Boolean>> = _registroState

    fun registrar(nombre: String, email: String, telefono: String, restaurante: String, direccion: String, pass: String) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || telefono.isEmpty() || restaurante.isEmpty() || direccion.isEmpty()) {
            _registroState.value = UiState.Error("Todos los campos son obligatorios")
            return
        }
        
        if (pass.length < 6) {
            _registroState.value = UiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        _registroState.value = UiState.Loading

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    val newUser = User(uid, nombre, email, telefono, restaurante, direccion)
                    
                    viewModelScope.launch {
                        try {
                            val guardado = firebaseService.guardarUsuario(newUser)
                            _registroState.value = if (guardado) UiState.Success(true) else UiState.Error("No se pudo guardar el perfil")
                        } catch (e: Exception) {
                            _registroState.value = UiState.Error(e.message ?: "Error en Firestore")
                        }
                    }
                } else {
                    val ex = task.exception
                    if (ex != null) CrashReporter.logError(ex, "Fallo Registro: $email")
                    _registroState.value = if (ex is FirebaseNetworkException) UiState.NoConnection else UiState.Error(ex?.message ?: "Error")
                }
            }
    }
}