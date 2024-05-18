package com.grid.pos

import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.grid.pos.interfaces.OnActivityResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.ui.navigation.AuthNavGraph
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val activityViewModel: ActivityScopedViewModel by viewModels()
    private var mActivityResultCallBack: OnActivityResult? = null
    private var mGalleryCallBack: OnGalleryResult? = null
    private var connectivityManager: ConnectivityManager? = null
    private val networkHandler = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            CoroutineScope(Dispatchers.IO).launch {
                activityViewModel.initiateValues()
            }
        }

        override fun onLost(network: Network) {
            Toast.makeText(
                this@MainActivity,
                "The application no longer has the ability to access the internet.",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
        ) {
        }

        override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties
        ) {
        }
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        mActivityResultCallBack?.onActivityResult(
            result.resultCode,
            result.data
        )
        mActivityResultCallBack = null
    }

    val pickSingleMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        uri?.let { mGalleryCallBack?.onGalleryResult(listOf(it)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        window.setBackgroundDrawableResource(R.drawable.white_background)
        setContent {
            val navController = rememberNavController()
            GridPOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthNavGraph(
                        modifier = Modifier
                            .background(color = White)
                            .padding(0.dp),
                        navController = navController,
                        activityViewModel = activityViewModel,
                        mainActivity = this,
                        startDestination = "LoginView" /*if (SettingsModel.currentUserId.isNullOrEmpty()) "LoginView" else "HomeView"*/
                    )
                }
            }
        }
    }

    fun launchActivityForResult(
            i: Intent,
            activityResult: OnActivityResult
    ) {
        try {
            mActivityResultCallBack = activityResult
            resultLauncher.launch(i)
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }

    fun launchGalleryPicker(
            mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            galleryResult: OnGalleryResult
    ) {
        try {
            mGalleryCallBack = galleryResult
            pickSingleMedia.launch(
                PickVisualMediaRequest(
                    mediaType
                )
            )
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        connectivityManager?.registerDefaultNetworkCallback(networkHandler)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager?.unregisterNetworkCallback(networkHandler)
    }
}