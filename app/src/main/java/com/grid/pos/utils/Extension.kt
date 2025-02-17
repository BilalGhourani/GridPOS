package com.grid.pos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Base64
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.grid.pos.App
import kotlinx.coroutines.launch
import java.sql.ResultSet
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Extension {
    fun String?.isNullOrEmptyOrNullStr(): Boolean {
        return this.isNullOrEmpty() || this.equals(
            "null",
            ignoreCase = true
        )
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
            detectDragGestures { change, _ ->
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

    fun ResultSet.getStringValue(
            value: String,
            fallback: String = ""
    ): String {
        return try {
            getString(value).takeIf { !it.isNullOrEmptyOrNullStr() } ?: fallback
        } catch (e: Exception) {
            //e.printStackTrace()
            fallback
        }
    }

    fun ResultSet.getDoubleValue(
            value: String,
            fallback: Double = 0.0
    ): Double {
        return try {
            getDouble(value).takeIf { !it.isNaN() } ?: fallback
        } catch (e: Exception) {
           //e.printStackTrace()
            fallback
        }
    }

    fun ResultSet.getLongValue(
            value: String,
            fallback: Long = 0
    ): Long {
        return try {
            getLong(value)
        } catch (e: Exception) {
            //e.printStackTrace()
            fallback
        }
    }

    fun ResultSet.getIntValue(
            value: String,
            fallback: Int = 0
    ): Int {
        return try {
            getInt(value)
        } catch (e: Exception) {
            //e.printStackTrace()
            fallback
        }
    }

    fun ResultSet.getBooleanValue(
            value: String,
            fallback: Boolean = false
    ): Boolean {
        return try {
            getBoolean(value)
        } catch (e: Exception) {
            //e.printStackTrace()
            fallback
        }
    }

    fun ResultSet.getObjectValue(
            value: String,
            fallback: Any? = null
    ): Any? {
        return try {
            getObject(value)
        } catch (e: Exception) {
            //e.printStackTrace()
            fallback
        }
    }

}