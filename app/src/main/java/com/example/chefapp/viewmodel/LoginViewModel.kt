package com.example.chefapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        object NoConnection : LoginState()    // Punto 7
        object SessionExpired : LoginState() // Punto 7 (No aplica en login pero sí en el flujo)
        data class Error(val message: String) : LoginState()
        data class Message(val message: String) : LoginState()
    }

    fun onLoginClicked(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _loginState.value = LoginState.Error("Campos incompletos")
            return
        }

        _loginState.value = LoginState.Loading

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value = LoginState.Success
                } else {
                    val exception = task.exception ?: Exception("Error desconocido")
                    com.example.chefapp.crash.CrashReporter.logError(exception, "Login Fallido: $email")
                    
                    if (exception is com.google.firebase.FirebaseNetworkException) {
                        _loginState.value = LoginState.NoConnection
                    } else {
                        _loginState.value = LoginState.Error(exception.message ?: "Error de autenticación")
                    }
                }
            }
    }

    fun onResetPasswordClicked(email: String) {
        if (email.isEmpty()) {
            _loginState.value = LoginState.Error("Ingresa tu correo para restablecer la contraseña")
            return
        }

        _loginState.value = LoginState.Loading

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value =
                        LoginState.Message("Se ha enviado un correo para restablecer tu contraseña")
                } else {
                    _loginState.value =
                        LoginState.Error(task.exception?.message ?: "Error al enviar el correo")
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

}