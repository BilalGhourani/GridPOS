package com.grid.pos

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        private lateinit var instance: App
        fun getInstance(): App {
            return instance
        }
    }

    private fun initFirebase() {
        // Initialize Firebase only once (preferably in onCreate)
        val options: FirebaseOptions = FirebaseOptions.Builder()
            .setApplicationId("1:337880577447:android:295a236f47063a5233b282")
            .setApiKey("AIzaSyDSh65g8EqvGeyOviwCKmJh4jFD2iXQhYk")
            .setProjectId("grids-app-8a2b7")
            .setDatabaseUrl("https://grids-app-8a2b7-default-rtdb.europe-west1.firebasedatabase.app")
            .build()
        FirebaseApp.initializeApp(this, options)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseFirestore.setLoggingEnabled(true)
    }
}