package com.grid.pos.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.grid.pos.R
import com.grid.pos.data.Item.Item
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.theme.GridPOSTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@AndroidEntryPoint
class BarcodeScannerActivity : ComponentActivity() {

    private val barcodeScannedList = mutableListOf<String>()
    private var stopScanning: Boolean = false
    private var scanToAdd: Boolean = false
    private var itemsMap: Map<String, Item>? = null
    private var mMediaPlayer: MediaPlayer? = null
    private val popupModelState = mutableStateOf(PopupModel())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.drawable.white_background)
        scanToAdd = intent.getBooleanExtra(
            "scanToAdd",
            false
        )
        val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "items",
                ArrayList::class.java
            ) as? ArrayList<Item>
        } else {
            intent.getSerializableExtra("items") as? ArrayList<Item>
        }
        itemsMap = items?.map { (it.itemBarcode ?: "-") to it }?.toMap()
        mMediaPlayer = MediaPlayer.create(
            this,
            com.google.zxing.client.android.R.raw.zxing_beep
        )
        setContent {
            var isPopupShown by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            GridPOSTheme {
                Scaffold(containerColor = SettingsModel.backgroundColor,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                            CameraPreviewView(cameraExecutor) { result ->
                                if (scanToAdd) {
                                    playScanSound()
                                    val item = itemsMap?.get(result)
                                    if (item != null) {
                                        if (!stopScanning) {
                                            stopScanning = true
                                            scope.launch {
                                                isPopupShown = true
                                                popupModelState.value = PopupModel().apply {
                                                    dialogTitle = null
                                                    dialogText = "Barcode already exist!"
                                                    icon = null
                                                    positiveBtnText = "Close"
                                                    negativeBtnText = null
                                                    height = 100.dp
                                                }
                                            }
                                        }
                                    } else {
                                        barcodeScannedList.clear()
                                        barcodeScannedList.add(result)
                                        finishScanning()
                                    }
                                } else {
                                    if (!stopScanning) {
                                        stopScanning = true
                                        playScanSound()
                                        if (itemsMap?.containsKey(result) == false) {
                                            scope.launch {
                                                isPopupShown = true
                                                popupModelState.value = PopupModel().apply {
                                                    dialogTitle = null
                                                    dialogText = "Barcode not exist!"
                                                    icon = null
                                                    positiveBtnText = "Close"
                                                    negativeBtnText = null
                                                    height = 100.dp
                                                }
                                            }

                                        } else {
                                            barcodeScannedList.add(result)
                                            scope.launch {
                                                isPopupShown = true
                                                popupModelState.value = PopupModel().apply {
                                                    dialogTitle = null
                                                    dialogText = "Scan next?"
                                                    icon = null
                                                    positiveBtnText = "Scan"
                                                    negativeBtnText = "Exit"
                                                    height = 100.dp
                                                }
                                                snackbarHostState.showSnackbar(
                                                    message = result,
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    AnimatedVisibility(
                        visible = isPopupShown,
                        enter = fadeIn(
                            initialAlpha = 0.4f
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 250)
                        )
                    ) {
                        UIAlertDialog(
                            onDismissRequest = {
                                isPopupShown = false
                                finishScanning()
                            },
                            onConfirmation = {
                                isPopupShown = false
                                stopScanning = false
                            },
                            popupModelState.value
                        )
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    finishScanning()
                }
            })
    }

    private fun finishScanning() {
        val returnIntent = Intent()
        returnIntent.putExtra(
            "SCAN_RESULTS",
            ArrayList(barcodeScannedList)
        )
        returnIntent.putExtra(
            "SCANNING_BARCODE",
            true
        )
        setResult(
            RESULT_OK,
            returnIntent
        )
        finish()
    }

    private fun playScanSound() {
        mMediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    @Composable
    fun CameraPreviewView(
            cameraExecutor: java.util.concurrent.ExecutorService,
            onResult: (String) -> Unit
    ) {
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                cameraProviderFuture.addListener(
                    {
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()

                        val imageAnalyzer = ImageAnalysis.Builder().build().also {
                            it.setAnalyzer(
                                cameraExecutor
                            ) { imageProxy ->
                                processImageProxy(
                                    barcodeScanner,
                                    imageProxy,
                                    onResult
                                )
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    },
                    ContextCompat.getMainExecutor(ctx)
                )

                previewView
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
            barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
            imageProxy: ImageProxy,
            onResult: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            barcodeScanner.process(image).addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_URL -> {
                            // Handle URL barcode
                        }

                        Barcode.TYPE_TEXT -> {
                            onResult.invoke(barcode.displayValue ?: "")
                        }

                        Barcode.TYPE_PRODUCT -> {
                            onResult.invoke(barcode.displayValue ?: "")
                        }
                        // Handle other types if needed
                    }
                }
            }.addOnFailureListener {
                // Handle failure
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }


    }
}

