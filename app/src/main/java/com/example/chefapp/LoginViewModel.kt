package com.example.chefapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun onLoginClicked(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _loginState.value = LoginState.Error("Por favor, completa todos los campos")
        } else {
            _loginState.value = LoginState.Loading

            _loginState.value = LoginState.Success
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}