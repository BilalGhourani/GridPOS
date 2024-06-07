package com.grid.pos.utils

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
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

object CryptoUtils {

    fun encrypt(
            text: String,
            key: String
    ): String {
        try {
            val crp = Cipher.getInstance("DESede/CBC/PKCS5Padding")
            val uEncode = Charsets.UTF_16
            val bytPlainText = text.toByteArray(uEncode)
            val stmCipherText = ByteArrayOutputStream()
            val slt = byteArrayOf(
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12
            )
            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec: KeySpec = PBEKeySpec(
                key.toCharArray(),
                slt,
                65536,
                192
            )
            val tmp: SecretKey = factory.generateSecret(spec)
            val secret: SecretKey = SecretKeySpec(
                tmp.encoded,
                "DESede"
            )

            crp.init(
                Cipher.ENCRYPT_MODE,
                secret,
                IvParameterSpec(
                    secret.encoded.copyOfRange(
                        0,
                        8
                    )
                )
            )

            val csEncrypted = CipherOutputStream(
                stmCipherText,
                crp
            )

            csEncrypted.write(bytPlainText)
            csEncrypted.flush()
            csEncrypted.close()
            return Base64.encodeToString(
                stmCipherText.toByteArray(),
                Base64.DEFAULT
            )
        } catch (ex: Exception) {
            Log.e(
                "exception",
                ex.message.toString()
            )
        }
        return text
    }

    fun decrypt(
            text: String,
            key: String
    ): String {
        try {
            val crp = Cipher.getInstance("DESede/CBC/PKCS5Padding")
            val uEncode = Charsets.UTF_16
            val bytCipherText = Base64.decode(
                text,
                Base64.DEFAULT
            )
            val stmPlainText = ByteArrayOutputStream()
            val stmCipherText = ByteArrayInputStream(bytCipherText)
            val slt = byteArrayOf(
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12
            )
            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec: KeySpec = PBEKeySpec(
                key.toCharArray(),
                slt,
                65536,
                192
            )
            val tmp: SecretKey = factory.generateSecret(spec)
            val secret: SecretKey = SecretKeySpec(
                tmp.encoded,
                "DESede"
            )

            crp.init(
                Cipher.DECRYPT_MODE,
                secret,
                IvParameterSpec(
                    secret.encoded.copyOfRange(
                        0,
                        8
                    )
                )
            )

            val csDecrypted = CipherInputStream(
                stmCipherText,
                crp
            )
            val reader = BufferedReader(
                InputStreamReader(
                    csDecrypted,
                    uEncode
                )
            )
            val writer = OutputStreamWriter(
                stmPlainText,
                uEncode
            )

            reader.forEachLine { writer.write(it) }
            writer.flush()

            return String(
                stmPlainText.toByteArray(),
                uEncode
            )
        } catch (ex: Exception) {
            Log.e(
                "exception",
                ex.message.toString()
            )
        }
        return text
    }

}