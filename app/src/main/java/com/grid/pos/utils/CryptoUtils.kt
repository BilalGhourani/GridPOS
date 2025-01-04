package com.grid.pos.utils

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

    private val salt = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private val uEncode = StandardCharsets.UTF_16LE
    private const val TRANSFORMATION = "DESede/CBC/PKCS5Padding"
    private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val SECRET_KEY_SPEC_ALGORITHM = "DESede"
    private const val ITERATOR_COUNT = 1000
    private const val KEY_LENGTH = 192// 192 bits for TripleDES

    fun encrypt(text: String, key: String): String {
        try {
            // Derive the key
            val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, ITERATOR_COUNT, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            val keyBytes = secretKey.encoded.copyOf(24) // TripleDES requires 24 bytes key

            // Create the key and IV
            val secretKeySpec: SecretKey = SecretKeySpec(keyBytes, SECRET_KEY_SPEC_ALGORITHM)

            // Convert text to bytes
            val plainTextBytes = text.toByteArray(uEncode)

            // Initialize the cipher
            val cipher = Cipher.getInstance(TRANSFORMATION)



            //val iv =byteArrayOf(-38, -66, -79, -86, -82, 39, 49, -38)
            val iv = Arrays.copyOfRange(keyBytes, 0, cipher.blockSize)

            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            // Encrypt the text
            val cipherTextBytes = cipher.doFinal(plainTextBytes)

            // Encode to Base64
            return java.util.Base64.getEncoder().encodeToString(cipherTextBytes)
                .trim()
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
            val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), salt, ITERATOR_COUNT, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            val keyBytes = secretKey.encoded.copyOf(24)
            val secretKeySpec = SecretKeySpec(keyBytes, SECRET_KEY_SPEC_ALGORITHM)


            val bytCipherText: ByteArray = java.util.Base64.getDecoder().decode(encryptedString)

            // Initialize the cipher
            val cipher = Cipher.getInstance(TRANSFORMATION)

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