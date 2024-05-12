package com.grid.pos

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.utils.Utils
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONException
import org.json.JSONObject

@HiltAndroidApp
class App : Application() {

    private var configs: JSONObject? = null

    companion object {
        private lateinit var instance: App
        fun getInstance(): App {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initAppConfig()
        FirebaseFirestore.setLoggingEnabled(true)
    }

    private fun initAppConfig() {
        val configString: String = Utils.readFileFromAssets("config.json", this)
        if (configString != null && !configString.isEmpty()) {
            try {
                configs = JSONObject(configString)
            } catch (e: JSONException) {
                Log.e("App", e.message.toString())
            }
        }
    }

    fun getConfigValue(
            key: String,
            fallback: String
    ): String {
        return configs?.optString(key, fallback) ?: fallback
    }

    private fun initFirebase() {
        // Initialize Firebase only once (preferably in onCreate)
        val options: FirebaseOptions = FirebaseOptions.Builder()
            .setApplicationId("1:337880577447:android:295a236f47063a5233b282")
            .setApiKey("AIzaSyDSh65g8EqvGeyOviwCKmJh4jFD2iXQhYk").setProjectId("grids-app-8a2b7")
            .setDatabaseUrl(
                "https://grids-app-8a2b7-default-rtdb.europe-west1.firebasedatabase.app"
            ).build()
        FirebaseApp.initializeApp(this, options)
    }

}