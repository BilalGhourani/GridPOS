package com.grid.pos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.grid.pos.interfaces.OnActivityResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.ui.login.LoginView
import com.grid.pos.ui.navigation.AuthNavGraph
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val activityViewModel: ActivityScopedViewModel by viewModels()
    private var mActivityResultCallBack: OnActivityResult? = null
    private var mGalleryCallBack: OnGalleryResult? = null

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mActivityResultCallBack?.onActivityResult(result.resultCode, result.data)
            mActivityResultCallBack = null
        }

    val pickSingleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            uri?.let { mGalleryCallBack?.onGalleryResult(listOf(it)) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.initValues()
        }
        window.setBackgroundDrawableResource(R.drawable.white_background)
        setContent {
            val navController = rememberNavController()
            GridPOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthNavGraph(
                        navController = navController,
                        activityViewModel = activityViewModel,
                        mainActivity = this,
                        startDestination = "LoginView"
                    )
                }
            }
        }
    }

    fun launchActivityForResult(i: Intent, activityResult: OnActivityResult) {
        try {
            mActivityResultCallBack = activityResult
            resultLauncher.launch(i)
        } catch (e: Exception) {
            Log.e("exception", e.message.toString())
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
            Log.e("exception", e.message.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GridPOSTheme {
        LoginView()
    }
}