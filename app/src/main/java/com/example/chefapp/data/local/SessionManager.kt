package com.example.chefapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ChefAppPrefs", Context.MODE_PRIVATE)

    fun saveLastEmail(email: String) {
        prefs.edit {
            putString("last_email", email)
        }
    }

    fun getLastEmail(): String? {
        return prefs.getString("last_email", null)
    }

    fun clearSession() {
        prefs.edit {
            remove("last_email")
        }
    }
}
