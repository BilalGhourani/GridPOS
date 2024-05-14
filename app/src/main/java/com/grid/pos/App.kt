package com.grid.pos

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
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
        initFirebase()
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
            .setApplicationId(SettingsModel.firebaseApplicationId!!)
            .setApiKey(SettingsModel.firebaseApiKey!!).setProjectId(SettingsModel.firebaseProjectId!!)
            .setDatabaseUrl(
                SettingsModel.firebaseDbPath!!
            ).build()
        FirebaseApp.initializeApp(this, options)
    }

}