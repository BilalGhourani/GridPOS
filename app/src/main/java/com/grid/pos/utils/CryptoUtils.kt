package com.grid.pos.utils

import android.R.attr
import android.os.Build
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.spec.KeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    val salt = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    val uEncode = StandardCharsets.UTF_16LE
    val transformation = "DESede/CBC/PKCS5Padding"
    val secretKetAlgorithm = "PBKDF2WithHmacSHA1"
    val SecretKeySpecAlgorithm = "DESede"
    val iterationCount = 1000
    val keyLength = 192// 192 bits for TripleDES

    fun encrypt(text: String, key: String): String {
        try {
            // Derive the key
            val factory = SecretKeyFactory.getInstance(secretKetAlgorithm)
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, iterationCount, keyLength)
            val secretKey = factory.generateSecret(spec)
            val keyBytes = secretKey.encoded.copyOf(24) // TripleDES requires 24 bytes key

            // Create the key and IV
            val secretKeySpec: SecretKey = SecretKeySpec(keyBytes, SecretKeySpecAlgorithm)

            // Convert text to bytes
            val plainTextBytes = text.toByteArray(uEncode)

            // Initialize the cipher
            val cipher = Cipher.getInstance(transformation)



            //val iv =byteArrayOf(-38, -66, -79, -86, -82, 39, 49, -38)
            val iv = Arrays.copyOfRange(keyBytes, 0, cipher.blockSize)

            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            // Encrypt the text
            val cipherTextBytes = cipher.doFinal(plainTextBytes)

            // Encode to Base64
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.util.Base64.getEncoder().encodeToString(cipherTextBytes)
            } else {
                String(Base64.encode(cipherTextBytes, Base64.DEFAULT))
            }.trim()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    fun decrypt(
        encryptedString: String,
        key: String
    ): String {
        try {
            // Derive the key
            val factory = SecretKeyFactory.getInstance(secretKetAlgorithm)
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, iterationCount, keyLength)
            val secretKey = factory.generateSecret(spec)
            val keyBytes = secretKey.encoded.copyOf(24)
            val secretKeySpec = SecretKeySpec(keyBytes, SecretKeySpecAlgorithm)


            val bytCipherText: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.util.Base64.getDecoder().decode(encryptedString)
            } else {
                Base64.decode(encryptedString, Base64.DEFAULT)
            }

            // Initialize the cipher
            val cipher = Cipher.getInstance(transformation)

            val iv = Arrays.copyOfRange(keyBytes, 0, cipher.blockSize)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

//            val cipherText = bytCipherText.copyOfRange(0, bytCipherText.size)

            // Decrypt the text
            val plainTextBytes = cipher.doFinal(bytCipherText)

            // Return uEncode.GetString(stmPlainText.ToArray())
            return String(plainTextBytes, uEncode)
        } catch (ex: Exception) {
             ex.printStackTrace()
        }
        return ""
    }

}