package com.grid.pos.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
        return String.format("#%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
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
        }.clickable(interactionSource, null) {}
    }

    fun String.encryptCBC(): String {
        val keyStr = App.getInstance().getConfigValue("key", "123456789")
        val ivStr = App.getInstance().getConfigValue("iv", "123456789")
        val iv = IvParameterSpec(ivStr.toByteArray())
        val keySpec = SecretKeySpec(keyStr.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val crypted = cipher.doFinal(this.toByteArray())
        val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
        return String(encodedByte)
    }

    fun String.decryptCBC(): String {
        val keyStr = App.getInstance().getConfigValue("key", "123456789")
        val ivStr = App.getInstance().getConfigValue("iv", "123456789")
        val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
        val iv = IvParameterSpec(ivStr.toByteArray())
        val keySpec = SecretKeySpec(keyStr.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }

    fun File.copy(
            dst: File,
            newName: String? = null
    ): String? {
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        var inChannel: FileChannel? = null
        var outChannel: FileChannel? = null

        try {
            inStream = FileInputStream(this)
            inChannel = inStream.channel
            val destination = newName?.let {
                File(dst.parent, newName)
            } ?: dst


            outStream = FileOutputStream(destination)
            outChannel = outStream.channel

            inChannel?.transferTo(0, inChannel.size(), outChannel)
            return destination.absolutePath
        } catch (e: Exception) {
            Log.e("copy_file", e.message.toString())
        } finally {
            try {
                outChannel?.close()
                outStream?.close()
            } catch (e: IOException) {
                Log.e("copy_file", e.message.toString())
            }
            try {
                inChannel?.close()
                inStream?.close()
            } catch (e: IOException) {
                Log.e("copy_file", e.message.toString())
            }
        }
        return null
    }

    fun File.copyAndGetPath(
            context: Context,
            dst: String,
            newName: String? = null
    ): String {
        try {
            val contentValues = ContentValues()
            val mimeType = "image/*"

            contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, this.path)
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newName)
            contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            contentValues.put(
                MediaStore.Files.FileColumns.OWNER_PACKAGE_NAME, context.getApplicationInfo().name
            )
            val contentResolver = context.contentResolver
            val destUriToInsert = FileProvider.getUriForFile(context, "com.grid.pos", File(dst))
            val uri = contentResolver.insert(Uri.parse(dst), contentValues)
            val fis: InputStream = if (this.exists()) {
                FileInputStream(this)
            } else {
                contentResolver.openInputStream(dst.toUri())!!
            }
            if (uri != null) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "rwt", null)
                if (parcelFileDescriptor != null) {
                    val fileOutputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
                    val input = ByteArray(1024)
                    var count: Int
                    while (fis.read(input).also { count = it } != -1) {
                        fileOutputStream.write(input, 0, count)
                    }
                    fis.close()
                    fileOutputStream.close()
                    contentValues.clear()
                    contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                    return uri.toString()
                }
            }
        } catch (e: IOException) {
            Log.e("copy_file",e.message.toString())
        }
        return ""
    }

}