package com.grid.pos

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.FileUtils
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application() {

    private var configs: JSONObject? = null
    private var isFirebaseInitialized: Boolean = false

    companion object {
        private lateinit var instance: App
        fun getInstance(): App {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this);
        CoroutineScope(Dispatchers.IO).launch {
            initAppConfig()
            DataStoreManager.initValues()
            initFirebase()
            FirebaseFirestore.setLoggingEnabled(true)
        }
    }

    private fun initAppConfig() {
        val configString: String = FileUtils.readFileFromAssets(
            "config.json",
            this
        )
        if (configString.isNotEmpty()) {
            try {
                configs = JSONObject(configString)
            } catch (e: JSONException) {
                Log.e(
                    "App",
                    e.message.toString()
                )
            }
        }
    }

    fun getConfigValue(
        key: String,
        fallback: String = ""
    ): String {
        return configs?.optString(
            key,
            fallback
        ) ?: fallback
    }

    fun initFirebase() {
        // Initialize Firebase only once (preferably in onCreate)
        try {
            if (!isFirebaseInitialized && !SettingsModel.firebaseApplicationId.isNullOrEmpty() && !SettingsModel.firebaseApiKey.isNullOrEmpty() && !SettingsModel.firebaseProjectId.isNullOrEmpty() && !SettingsModel.firebaseDbPath.isNullOrEmpty()) {
                val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId(SettingsModel.firebaseApplicationId!!)
                    .setApiKey(SettingsModel.firebaseApiKey!!)
                    .setProjectId(SettingsModel.firebaseProjectId!!).setDatabaseUrl(
                        SettingsModel.firebaseDbPath!!
                    ).build()
                FirebaseApp.initializeApp(
                    this,
                    options
                )
                isFirebaseInitialized = true
            }
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }

    fun isFirebaseInitiated(): Boolean {
        return isFirebaseInitialized
    }

    fun isMissingFirebaseConnection(): Boolean {
        return SettingsModel.isConnectedToFireStore() && !isFirebaseInitialized
    }

    fun killTheApp() {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).appTasks.forEach {
            it.finishAndRemoveTask()
        }
        exitProcess(0)
    }

}