package com.grid.pos.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintAttributes.MediaSize
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import com.aspose.cells.SaveFormat
import com.aspose.cells.Workbook
import com.grid.pos.MainActivity
import com.grid.pos.data.DataModel
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.ConnectionModel
import com.grid.pos.model.HomeSectionModel
import com.grid.pos.model.InvoiceItemModel
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object FileUtils {
    fun saveToInternalStorage(
        context: Context,
        parent: String = "family",
        sourceFilePath: Uri,
        destName: String
    ): String? {
        val storageDir = File(
            context.filesDir,
            "images"
        )
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val parentDir = File(
            storageDir,
            parent
        )
        if (!parentDir.exists()) {
            parentDir.mkdir()
        }
        val name = "$destName.jpg"
        val destinationFile = File(
            parentDir,
            name
        )

        val contentResolver = context.contentResolver
        try {
            val sourceFile = File(sourceFilePath.toString())
            val inputStream: InputStream = if (!sourceFile.exists()) {
                // Opening from gallery using content URI
                contentResolver.openInputStream(sourceFilePath)!!
            } else {
                // Opening from internal storage using path
                FileInputStream(sourceFile)
            }
            val outputStream = destinationFile.outputStream()
            val buffer = ByteArray(1024) // Adjust buffer size as needed
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                outputStream.write(
                    buffer,
                    0,
                    bytesRead
                )
            }
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Log.e(
                "tag",
                "Failed to copy image",
                e
            )
        }
        return destinationFile.absolutePath
    }

    fun saveToExternalStorage(
        context: Context,
        parent: String = "family",
        sourceFilePath: Uri,
        destName: String,
        type: String = "Image",
        workbook: Workbook? = null
    ): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, destName)
            put(
                MediaStore.MediaColumns.MIME_TYPE,
                getMimeTypeFromFileExtension(sourceFilePath.toString(), type)
            )
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "Android/media/${context.packageName}/$parent"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (type) {
                "excel" -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                else -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        } else {
            when (type) {
                "excel" -> {
                    MediaStore.Files.getContentUri(Environment.DIRECTORY_DOWNLOADS)
                }

                else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        }

        val insertedUri: Uri? = resolver.insert(collection, contentValues)

        insertedUri?.let { uri ->
            try {
                val contentResolver = context.contentResolver
                workbook?.let { workbook ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        workbook.save(
                            outputStream,
                            SaveFormat.XLSX
                        )
                        outputStream.close()
                    }
                } ?: run {
                    val sourceFile = File(sourceFilePath.toString())
                    val inputStream: InputStream = if (!sourceFile.exists()) {
                        // Opening from gallery using content URI
                        contentResolver.openInputStream(sourceFilePath)!!
                    } else {
                        // Opening from internal storage using path
                        FileInputStream(sourceFile)
                    }
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        val buffer = ByteArray(1024) // Adjust buffer size as needed
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                            outputStream.write(
                                buffer,
                                0,
                                bytesRead
                            )
                        }
                        inputStream.close()
                        outputStream.close()
                    }
                }

            } catch (e: IOException) {
                Log.e(
                    "tag",
                    "Failed to copy image",
                    e
                )
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            return uri.toString()
        }
        return null
    }

    fun getFileFromUri(context: Context, uri: Uri): File {
        val cursor =
            context.contentResolver.query(uri, arrayOf(MediaStore.Downloads.DATA), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Downloads.DATA))
                return File(filePath)
            }
        }
        return uri.toFile()
    }

    fun getMimeTypeFromFileExtension(filePath: String, type: String = "Image"): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        val fallback = when (type) {
            "excel" -> "application/vnd.ms-excel"
            else -> "image/jpeg"
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: fallback
    }


    fun readFileFromAssets(
        fileName: String,
        context: Context
    ): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            stringBuilder.toString()
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            ""
        }
    }


    fun getDefaultReceipt(): String {
        return "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + "    <title>Receipt</title>\n" + "    <style>\n" + "        body {\n" + "            font-family: Arial, sans-serif;\n" + "            margin: 0;\n" + "            padding: 20px;\n" + "        }\n" + "        .container {\n" + "            max-width: 400px;\n" + "            margin: 0 auto;\n" + "            border: 1px solid #ccc;\n" + "            padding: 20px;\n" + "            border-radius: 5px;\n" + "        }\n" + "        .receipt-header {\n" + "            text-align: center;\n" + "            margin-bottom: 20px;\n" + "        }\n" + "        .receipt-items {\n" + "            border-collapse: collapse;\n" + "            width: 100%;\n" + "        }\n" + "        .receipt-items th, .receipt-items td {\n" + "            border: 1px solid #ddd;\n" + "            padding: 8px;\n" + "            text-align: left;\n" + "        }\n" + "        .receipt-items th {\n" + "            background-color: #f2f2f2;\n" + "        }\n" + "        .total {\n" + "            margin-top: 20px;\n" + "            text-align: right;\n" + "        }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "<div class=\"container\">\n" + "    <div class=\"receipt-header\">\n" + "        <h2>Receipt</h2>\n" + "    </div>\n" + "    <table class=\"receipt-items\">\n" + "        <thead>\n" + "        <tr>\n" + "            <th>Item</th>\n" + "            <th>Quantity</th>\n" + "            <th>Price</th>\n" + "        </tr>\n" + "        </thead>\n" + "        <tbody>\n" + "        {rows_content}\n" + "        </tbody>\n" + "    </table>\n" + "    <div class=\"total\">\n" + "        <strong>Total: {total}</strong>\n" + "    </div>\n" + "</div>\n" + "</body>\n" + "</html>"
    }

    fun getDefaultItemReceipt(): String {
        return "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + "    <title>Receipt</title>\n" + "    <style>\n" + "        body {\n" + "            font-family: Arial, sans-serif;\n" + "            margin: 0;\n" + "            padding: 20px;\n" + "        }\n" + "        .container {\n" + "            max-width: 400px;\n" + "            margin: 0 auto;\n" + "            border: 1px solid #ccc;\n" + "            padding: 20px;\n" + "            border-radius: 5px;\n" + "        }\n" + "        .receipt-header {\n" + "            text-align: center;\n" + "            margin-bottom: 20px;\n" + "        }\n" + "        .receipt-items {\n" + "            border-collapse: collapse;\n" + "            width: 100%;\n" + "        }\n" + "        .receipt-items th, .receipt-items td {\n" + "            border: 1px solid #ddd;\n" + "            padding: 8px;\n" + "            text-align: left;\n" + "        }\n" + "        .receipt-items th {\n" + "            background-color: #f2f2f2;\n" + "        }\n" + "        .total {\n" + "            margin-top: 20px;\n" + "            text-align: right;\n" + "        }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "<div class=\"container\">\n" + "    <div class=\"receipt-header\">\n" + "        <h2>Receipt</h2>\n" + "    </div>\n" + "    <table class=\"receipt-items\">\n" + "        <thead>\n" + "        <tr>\n" + "            <th>Item</th>\n" + "            <th>Quantity</th>\n" + "            <th>Price</th>\n" + "        </tr>\n" + "        </thead>\n" + "        <tbody>\n" + "        {rows_content}\n" + "        </tbody>\n" + "    </table>\n" + "    <div class=\"total\">\n" + "        <strong>Total: {total}</strong>\n" + "    </div>\n" + "</div>\n" + "</body>\n" + "</html>"
    }

}