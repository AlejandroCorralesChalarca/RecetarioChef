package com.example.chefapp.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Clase encargada de centralizar el registro de errores en Firebase Crashlytics.
 */
object CrashReporter {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logError(e: Throwable, message: String? = null) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(e)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }
    
    fun setUserId(id: String) {
        crashlytics.setUserId(id)
    }
}
