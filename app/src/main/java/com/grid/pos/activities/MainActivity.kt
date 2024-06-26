package com.grid.pos.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.grid.pos.ActivityScopedUIEvent
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.interfaces.OnActivityResult
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.ui.navigation.AuthNavGraph
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.Extension.getStoragePermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val activityViewModel: ActivityScopedViewModel by viewModels()
    private var mActivityResultCallBack: OnActivityResult? = null
    private var mGalleryCallBack: OnGalleryResult? = null
    private var mOnBarcodeResult: OnBarcodeResult? = null
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

    var permissionDelegate: ((Boolean) -> Unit)? = null

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionDelegate?.invoke(isGranted)
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

    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data?.extras?.containsKey("SCANNING_BARCODE") == true) {
                val result = result.data?.extras!!.getString(
                    "SCAN_RESULT",
                    ""
                )
                mOnBarcodeResult?.OnBarcodeResult(result)
            } else {
                val data = result.data?.data
                data?.let { mGalleryCallBack?.onGalleryResult(listOf(it)) }
            }
        }
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
                        startDestination = "LicenseView" /*if (SettingsModel.currentUserId.isNullOrEmpty()) "LoginView" else "HomeView"*/
                    )
                }
            }
        }
        registerActivityScopedEvent()
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

    fun openAppStorageSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
        }
        startActivity(intent)
    }

    private fun registerActivityScopedEvent() {
        activityViewModel.mainActivityEvent.onEach { sharedEvent ->
            when (sharedEvent) {
                is ActivityScopedUIEvent.Finish -> {
                    this@MainActivity.finish()
                }

                is ActivityScopedUIEvent.OpenAppSettings -> {
                    openAppStorageSettings()
                }

                is ActivityScopedUIEvent.LaunchGalleryPicker -> {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            getStoragePermissions()
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionDelegate = { granted ->
                            if (granted) {
                                launchGalleryPicker(
                                    sharedEvent.mediaType,
                                    sharedEvent.delegate
                                )
                            } else {
                                sharedEvent.onPermissionDenied.invoke()
                            }
                        }
                        requestStoragePermission.launch(getStoragePermissions())
                    } else {
                        launchGalleryPicker(
                            sharedEvent.mediaType,
                            sharedEvent.delegate
                        )
                    }
                }

                is ActivityScopedUIEvent.LaunchFilePicker -> {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            getStoragePermissions()
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionDelegate = { granted ->
                            if (granted) {
                                launchFilePicker(sharedEvent.delegate)
                            } else {
                                sharedEvent.onPermissionDenied.invoke()
                            }
                        }
                        requestStoragePermission.launch(getStoragePermissions())
                    } else {
                        launchFilePicker(sharedEvent.delegate)
                    }

                }

                is ActivityScopedUIEvent.StartChooserActivity -> {
                    startActivity(sharedEvent.intent)
                }

                is ActivityScopedUIEvent.LaunchBarcodeScanner -> {
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionDelegate = { granted ->
                            if (granted) {
                                launchCameraActivity(sharedEvent.delegate)
                            } else {
                                sharedEvent.onPermissionDenied.invoke()
                            }
                        }
                        requestStoragePermission.launch(permission)
                    } else {
                        launchCameraActivity(sharedEvent.delegate)
                    }

                }
            }
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun launchFilePicker(delegate: OnGalleryResult) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "application/octet-stream",
                    "application/x-sqlite3",
                    "application/vnd.sqlite3",
                    "application/x-sqlite3"
                )
            )

            /*  val pickerInitialUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
              } else {
                  MediaStore.Files.getContentUri(Environment.DIRECTORY_DOWNLOADS)

              }
              // Optionally, specify a URI for the file that should appear in the
              // system file picker when it loads.
              putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)*/
        }
        startForResult.launch(
            Intent.createChooser(
                intent,
                "Select DB File"
            )
        )
        mGalleryCallBack = delegate
    }

    private fun launchCameraActivity(delegate: OnBarcodeResult) {
        mOnBarcodeResult = delegate
        val intent = Intent(
            this,
            BarcodeScannerActivity::class.java
        )
        startForResult.launch(intent)
    }
}

/*
when (SettingsModel.connectionType) {
    CONNECTION_TYPE.FIRESTORE.key -> {

    }
    CONNECTION_TYPE.LOCAL.key -> {

    }
    else ->{

    }
}*/