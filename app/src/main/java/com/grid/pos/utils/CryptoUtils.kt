package com.grid.pos.utils

import android.os.Build
import android.util.Base64
import android.util.Log
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    fun encrypt(text: String, key: String): String {
        try {
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
        } catch (ex: Exception) {
            Log.e(
                "exception",
                ex.message.toString()
            )
        }
        return ""
    }

    fun decrypt(
        encryptedString: String,
        key: String
    ): String {
        try {
            // crp = New TripleDESCryptoServiceProvider
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

            // Dim uEncode As New UnicodeEncoding
            val uEncode = StandardCharsets.UTF_16

            // Dim bytCipherText() As Byte = Convert.FromBase64String(text)
            val bytCipherText: ByteArray = Base64.decode(encryptedString,Base64.DEFAULT)

            // Dim stmPlainText As New MemoryStream
            // Not directly needed in Kotlin, handled via ByteArray

            // Dim stmCipherText As New MemoryStream(bytCipherText)
            // Not directly needed in Kotlin, handled via ByteArray

            // Dim slt() As Byte = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}
            val salt = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

            // Dim pdb As New Rfc2898DeriveBytes(key, slt)
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, 1000, 192)
            val tmp = factory.generateSecret(spec)

            // Dim bytDerivedKey() As Byte = pdb.GetBytes(24)
            val secretKey = SecretKeySpec(tmp.encoded, "DESede")

            // crp.Key = bytDerivedKey
            // crp.IV = pdb.GetBytes(8)
            val iv =  bytCipherText.copyOfRange(0, 8)

            // Dim csDecrypted As New CryptoStream(stmCipherText, crp.CreateDecryptor(), CryptoStreamMode.Read)
            val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

            // Decrypt the text
            val plainTextBytes = cipher.doFinal(bytCipherText.copyOfRange(iv.size, bytCipherText.size))

            // Return uEncode.GetString(stmPlainText.ToArray())
            return String(plainTextBytes, uEncode)
        } catch (ex: Exception) {
             ex.printStackTrace()
        }
        return ""
    }

}