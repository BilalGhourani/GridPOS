package com.grid.pos.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.grid.pos.ui.theme.GridPOSTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors

@AndroidEntryPoint
class BarcodeScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.drawable.white_background)
        setContent {
            GridPOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                        CameraPreviewView(cameraExecutor) { result ->
                            val returnIntent = Intent()
                            returnIntent.putExtra(
                                "SCAN_RESULT",
                                result
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
                    }

                }
            }
        }
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
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.3f),
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

