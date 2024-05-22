package com.grid.pos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.StringUtils
import com.grid.pos.App
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Extension {
    fun Long?.isNullOrZero(): Boolean {
        return this != null && this != 0L
    }

    fun Float.toColorInt(): Int = (this * 255 + 0.5f).toInt()

    fun Color.toHexCode(): String {
        val red = this.red * 255
        val green = this.green * 255
        val blue = this.blue * 255
        return String.format(
            "#%02x%02x%02x",
            red.toInt(),
            green.toInt(),
            blue.toInt()
        )
    }

    @SuppressLint("ModifierFactoryUnreferencedReceiver")
    fun Modifier.emitDragGesture(
            interactionSource: MutableInteractionSource
    ): Modifier = composed {
        val scope = rememberCoroutineScope()
        pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                scope.launch {
                    interactionSource.emit(PressInteraction.Press(change.position))
                }
            }
        }.clickable(
            interactionSource,
            null
        ) {}
    }

    fun String.encryptCBC(): String {
        val keyStr = App.getInstance().getConfigValue(
            "key",
            "123456789"
        )
        val ivStr = App.getInstance().getConfigValue(
            "iv",
            "123456789"
        )
        val iv = IvParameterSpec(ivStr.toByteArray())
        val keySpec = SecretKeySpec(
            keyStr.toByteArray(),
            "AES"
        )
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            keySpec,
            iv
        )
        val crypted = cipher.doFinal(this.toByteArray())
        val encodedByte = Base64.encode(
            crypted,
            Base64.DEFAULT
        )
        return String(encodedByte).trim()
    }

    fun String.decryptCBC(): String {
        val keyStr = App.getInstance().getConfigValue(
            "key",
            "123456789"
        )
        val ivStr = App.getInstance().getConfigValue(
            "iv",
            "123456789"
        )
        val decodedByte: ByteArray = Base64.decode(
            this,
            Base64.DEFAULT
        )
        val iv = IvParameterSpec(ivStr.toByteArray())
        val keySpec = SecretKeySpec(
            keyStr.toByteArray(),
            "AES"
        )
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(
            Cipher.DECRYPT_MODE,
            keySpec,
            iv
        )
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }

    fun Context.isPermissionGranted(name: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            name
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Activity.shouldShowRationale(name: String): Boolean {
        return shouldShowRequestPermissionRationale(name)
    }

    fun Context.getStoragePermissions(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Manifest.permission.READ_MEDIA_IMAGES
            }

            else -> {
                Manifest.permission.READ_EXTERNAL_STORAGE + ","
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        }
    }

    fun Context.hasPickMediaPermission(): Boolean {
        return isPermissionGranted(getStoragePermissions())
    }

    fun Context.gotoApplicationSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:${packageName}")
        })
    }

    fun Context.findActivity(): Activity? {
        return when (this) {
            is Activity -> this
            is ContextWrapper -> {
                baseContext.findActivity()
            }

            else -> null
        }
    }

}