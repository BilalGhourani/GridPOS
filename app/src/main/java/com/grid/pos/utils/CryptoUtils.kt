package com.grid.pos.utils

import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log
import okio.utf8Size
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

object CryptoUtils {

    fun encrypt(text: String, key: String): String {
        // Salt for the key derivation (must match the salt used during decryption)
        val salt = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

        // Derive the key
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, 1000, 192)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "DESede")

        // Generate a random IV
        val iv = ByteArray(8)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        // Initialize the cipher
        val cipher = Cipher.getInstance("DESede/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        // Encrypt the text
        val plainTextBytes = text.toByteArray(StandardCharsets.UTF_8)
        val cipherTextBytes = cipher.doFinal(plainTextBytes)

        // Combine IV and cipherText
        val combined = ByteArray(iv.size + cipherTextBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherTextBytes, 0, combined, iv.size, cipherTextBytes.size)

        // Encode to Base64
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encodeToString(combined)
        } else {
            String(Base64.encode(combined, Base64.DEFAULT))
        }
    }

    fun decrypt(
        text: String,
        key: String
    ): String {
        // Salt for the key derivation (must match the salt used during encryption)
        val salt = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

        // Derive the key
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, 1000, 192)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "DESede")

        // Decode the base64 encoded text
        val cipherText =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getDecoder().decode(text)
        } else {
           Base64.decode(text, Base64.DEFAULT)
        }

        // Extract the IV (first 8 bytes of the cipherText)
        val iv = cipherText.copyOfRange(0, 8)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        // Decrypt the text
        val plainTextBytes = cipher.doFinal(cipherText.copyOfRange(8, cipherText.size))
        return String(plainTextBytes, StandardCharsets.UTF_8)
    }

}