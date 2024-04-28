package com.grid.pos

import android.app.Application
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

    private lateinit var database: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}