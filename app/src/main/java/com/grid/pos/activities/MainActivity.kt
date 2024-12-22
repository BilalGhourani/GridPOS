package com.grid.pos.activities

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.grid.pos.ActivityUIEvent
import com.grid.pos.SharedViewModel
import com.grid.pos.R
import com.grid.pos.data.item.Item
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.interfaces.OnActivityResult
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.ORIENTATION_TYPE
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.navigation.AuthNavGraph
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.Extension.getStoragePermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    private var mActivityResultCallBack: OnActivityResult? = null
    private var mGalleryCallBack: OnGalleryResult? = null
    private var mOnBarcodeResult: OnBarcodeResult? = null
    private var connectivityManager: ConnectivityManager? = null
    private val networkHandler = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            CoroutineScope(Dispatchers.IO).launch {
                sharedViewModel.initiateValues()
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

    private var permissionDelegate: ((Boolean) -> Unit)? = null

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

    private val pickSingleMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        uri?.let { mGalleryCallBack?.onGalleryResult(listOf(it)) }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data?.extras?.containsKey("SCANNING_BARCODE") == true) {
                if (result.data?.extras?.getBoolean("scanToAdd") == true) {
                    val barcodes = result.data?.extras?.getStringArrayList(
                        "SCAN_RESULTS"
                    ) ?: listOf()
                    mOnBarcodeResult?.OnBarcodeResult(barcodes)
                } else {
                    val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.extras?.getSerializable(
                            "SCAN_RESULTS",
                            ArrayList::class.java
                        )
                    } else {
                        result.data?.extras?.getSerializable("SCAN_RESULTS") as? ArrayList<*>
                    }
                    mOnBarcodeResult?.OnBarcodeResult(items?.toList() ?: listOf())
                }
            } else {
                val data = result.data?.data
                data?.let { mGalleryCallBack?.onGalleryResult(listOf(it)) }
            }
        }
    }
    private val loadingState = mutableStateOf(false)
    private val popupState = mutableStateOf(false)
    private var popupModel: PopupModel? = null
    private var mLoaderHandler: Handler? = null
    private val mLoaderRunnable: Runnable = Runnable {
        loadingState.value = false
        popupModel = PopupModel(
            dialogText = "Timeout exceeded!",
            positiveBtnText = "Close",
            negativeBtnText = null
        )
        popupState.value = true
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
                        sharedViewModel = sharedViewModel,
                        startDestination = "LoginView"
                    )
                    AnimatedVisibility(
                        visible = popupState.value,
                        enter = fadeIn(
                            initialAlpha = 0.4f
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 250)
                        )
                    ) {
                        UIAlertDialog(
                            onConfirmation = {
                                popupState.value = false
                                popupModel?.onConfirmation?.invoke()
                            },
                            onDismissRequest = {
                                popupState.value = false
                                popupModel?.onDismissRequest?.invoke()
                            },
                            popupModel = popupModel ?: PopupModel()
                        )
                    }
                    LoadingIndicator(
                        show = loadingState.value
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

    private fun launchGalleryPicker(
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
        changeOrientationType(SettingsModel.orientationType)
        connectivityManager?.registerDefaultNetworkCallback(networkHandler)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager?.unregisterNetworkCallback(networkHandler)
    }

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.IO).launch {
            SQLServerWrapper.closeConnection()
        }
    }

    private fun openAppStorageSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
        }
        startActivity(intent)
    }

    private fun registerActivityScopedEvent() {
        sharedViewModel.mainActivityEvent.onEach { sharedEvent ->
            when (sharedEvent) {
                is ActivityUIEvent.Finish -> {
                    this@MainActivity.finish()
                }

                is ActivityUIEvent.ShowLoading -> {
                    mLoaderHandler?.removeCallbacks(mLoaderRunnable)
                    mLoaderHandler = null
                    if (sharedEvent.show && sharedEvent.timeout > 0) {
                        mLoaderHandler = Handler(Looper.getMainLooper())
                        mLoaderHandler!!.postDelayed(
                            mLoaderRunnable,
                            sharedEvent.timeout
                        )
                    }
                    loadingState.value = sharedEvent.show
                }

                is ActivityUIEvent.ShowPopup -> {
                    if (popupState.value != sharedEvent.show) {
                        popupModel = sharedEvent.popupModel
                        popupState.value = sharedEvent.show
                    }
                }

                is ActivityUIEvent.OpenAppSettings -> {
                    openAppStorageSettings()
                }

                is ActivityUIEvent.LaunchGalleryPicker -> {
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

                is ActivityUIEvent.LaunchFilePicker -> {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            getStoragePermissions()
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionDelegate = { granted ->
                            if (granted) {
                                launchFilePicker(
                                    sharedEvent.intentType,
                                    sharedEvent.delegate
                                )
                            } else {
                                sharedEvent.onPermissionDenied.invoke()
                            }
                        }
                        requestStoragePermission.launch(getStoragePermissions())
                    } else {
                        launchFilePicker(
                            sharedEvent.intentType,
                            sharedEvent.delegate
                        )
                    }

                }

                is ActivityUIEvent.StartChooserActivity -> {
                    startActivity(sharedEvent.intent)
                }

                is ActivityUIEvent.LaunchBarcodeScanner -> {
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionDelegate = { granted ->
                            if (granted) {
                                launchCameraActivity(
                                    sharedEvent.scanToAdd,
                                    sharedEvent.items,
                                    sharedEvent.delegate
                                )
                            } else {
                                sharedEvent.onPermissionDenied.invoke()
                            }
                        }
                        requestStoragePermission.launch(permission)
                    } else {
                        launchCameraActivity(
                            sharedEvent.scanToAdd,
                            sharedEvent.items,
                            sharedEvent.delegate
                        )
                    }

                }

                is ActivityUIEvent.ChangeAppOrientation -> {
                    changeOrientationType(sharedEvent.orientationType)
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun changeOrientationType(orientationType: String) {
        val orientation = when (orientationType) {
            ORIENTATION_TYPE.PORTRAIT.key -> {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            ORIENTATION_TYPE.LANDSCAPE.key -> {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            else -> {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        requestedOrientation = orientation
    }

    private fun launchFilePicker(
            intentType: String,
            delegate: OnGalleryResult
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = intentType
            if (intentType == "*/*") {
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "application/octet-stream",
                        "application/x-sqlite3",
                        "application/vnd.sqlite3",
                        "application/x-sqlite3"
                    )
                )
            }

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

    private fun launchCameraActivity(
            scanToAdd: Boolean,
            items: ArrayList<Item>?,
            delegate: OnBarcodeResult
    ) {
        mOnBarcodeResult = delegate
        val intent = Intent(
            this,
            BarcodeScannerActivity::class.java
        )
        intent.putExtra(
            "scanToAdd",
            scanToAdd
        )
        items?.let {
            intent.putExtra(
                "items",
                it
            )
        }

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
