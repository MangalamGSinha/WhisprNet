// WhisprNetApplication.kt
package com.example.whisprnet

import android.app.Application
import com.google.firebase.FirebaseApp

class WhisprNetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
