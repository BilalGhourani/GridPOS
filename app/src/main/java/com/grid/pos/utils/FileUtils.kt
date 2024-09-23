package com.grid.pos.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.grid.pos.App
import com.grid.pos.data.AppDatabase
import com.grid.pos.di.AppModule
import com.grid.pos.model.FileModel
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Date

object FileUtils {
    fun saveToInternalStorage(
            context: Context,
            parent: String,
            sourceFilePath: Uri,
            destName: String
    ): String? {
        val storageDir = File(
            context.filesDir,
            parent
        )
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val destinationFile = File(
            storageDir,
            destName
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
            workbook: XSSFWorkbook? = null
    ): String? {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.DownloadColumns.DISPLAY_NAME,
                    destName
                )
                put(
                    MediaStore.DownloadColumns.MIME_TYPE,
                    getMimeTypeFromFileExtension(
                        sourceFilePath.toString(),
                        type
                    )
                )
                put(
                    MediaStore.DownloadColumns.RELATIVE_PATH,
                    "Download/${context.packageName}/$parent"
                )
                put(
                    MediaStore.DownloadColumns.IS_PENDING,
                    1
                )
            }

            val collection = MediaStore.Downloads.getContentUri("external")

            val insertedUri: Uri? = resolver.insert(
                collection,
                contentValues
            )

            insertedUri?.let { uri ->
                try {
                    val contentResolver = context.contentResolver
                    workbook?.let { workbook ->
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            workbook.write(
                                outputStream
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
                        contentValues.put(
                            MediaStore.DownloadColumns.IS_PENDING,
                            0
                        )
                        resolver.update(
                            uri,
                            contentValues,
                            null,
                            null
                        )
                    }
                }
                return uri.toString()
            }
        } else {

            val mediaDir: File? = context.getExternalFilesDir(null)?.parentFile?.let {
                File(
                    it,
                    "Android/media/${context.packageName}/$parent"
                )
            }

            if (mediaDir != null && !mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val destFile = File(
                mediaDir,
                destName
            )
            val outputStream: OutputStream? = resolver.openOutputStream(Uri.fromFile(destFile))

            resolver.openInputStream(sourceFilePath)?.use { inputStream ->
                outputStream?.use { output ->
                    inputStream.copyTo(output)
                }
            }
            return destFile.path
        }
        return null
    }

    fun getFileContent(
            context: Context,
            uri: Uri
    ): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

        val size = inputStream?.available() ?: 0

        val bytes = ByteArray(size)

        inputStream?.read(bytes)

        inputStream?.close()

        return String(
            bytes,
            StandardCharsets.UTF_8
        )
    }

    fun deleteFile(
            context: Context,
            path: String
    ) {
        if (path.startsWith("content")) {
            val file: DocumentFile? = DocumentFile.fromSingleUri(
                context,
                Uri.parse(path)
            )
            file?.delete()
        } else {
            val file = File(path)
            file.deleteOnExit()
        }
    }

    private fun getMimeTypeFromFileExtension(
            filePath: String,
            type: String = "Image"
    ): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        val fallback = when (type) {
            "license" -> "*/*"//"text/plain"
            "excel" -> "application/vnd.ms-excel"
            "sqlite" -> "application/octet-stream"
            "html" -> "text/html"
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

    private fun copyFile(
            fromFile: File,
            toFile: File
    ) {
        try {
            val sourceChannel = FileInputStream(fromFile).channel
            val targetChannel = FileOutputStream(toFile).channel
            targetChannel.transferFrom(
                sourceChannel,
                0,
                sourceChannel.size()
            )
            sourceChannel.close()
            targetChannel.close()
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }

    private fun copyFile(
            context: Context,
            fromFile: Uri,
            toFile: File
    ) {
        try {
            val contentResolver = context.contentResolver
            val sourceFile = File(fromFile.toString())
            val inputStream: InputStream = if (!sourceFile.exists()) {
                // Opening from gallery using content URI
                contentResolver.openInputStream(fromFile)!!
            } else {
                // Opening from internal storage using path
                FileInputStream(sourceFile)
            }
            val outputStream = toFile.outputStream()
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
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }

    fun backup() {
        val app = App.getInstance()
        val appDatabase: AppDatabase = AppModule.provideDatabase(app)
        appDatabase.close()
        val dbFile: File = app.getDatabasePath(Constants.DATABASE_NAME)
        saveToExternalStorage(
            context = app.applicationContext,
            parent = "bachup",
            sourceFilePath = dbFile.toUri(),
            destName = "grids ${
                DateHelper.getDateInFormat(
                    Date(),
                    "dd MMM yyyy hh_mm_ss"
                )
            }.db",
            type = "sqlite"
        )
    }

    fun restore(
            context: Context,
            uri: Uri
    ) {
        val app = App.getInstance()
        var dbFile: File = context.getDatabasePath(Constants.DATABASE_NAME)
        dbFile.delete()
        val appDatabase: AppDatabase = AppModule.provideDatabase(app)
        appDatabase.close()
        dbFile = context.getDatabasePath(Constants.DATABASE_NAME)
        copyFile(
            context,
            uri,
            dbFile
        )
    }

    private fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            val appCacheDir = File(cacheDir.parent!!)
            if (appCacheDir.exists()) {
                val children = appCacheDir.list()
                if (children != null) {
                    for (child in children) {
                        if (child != "lib") {
                            deleteDir(
                                File(
                                    appCacheDir,
                                    child
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    val success = deleteDir(
                        File(
                            dir,
                            child
                        )
                    )
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir?.delete() ?: false
    }

    fun getLastWriteTimeFromUri(
            context: Context,
            uri: Uri
    ): Date? {
        val file = getFileFromUri(
            context,
            uri
        )
        if (file?.exists() == true) {
            return Date(file.lastModified())
        }
        return null
    }

    fun getFileFromUri(
            context: Context,
            uri: Uri
    ): File? {
        val filePath: String = getFilePathFromUri(
            context,
            uri
        )
        val file = File(filePath)
        if (file.exists()) {
            return file
        }
        return null
    }

    private fun getFilePathFromUri(
            context: Context,
            uri: Uri
    ): String {
        return if (DocumentsContract.isDocumentUri(
                context,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                if ("primary".equals(
                        split[0],
                        ignoreCase = true
                    )
                ) {
                    "${context.getExternalFilesDir(null)}/${split[1]}"
                } else {
                    uri.toString()
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (id != null) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    getDataColumn(
                        context,
                        contentUri,
                        null,
                        null
                    )
                } else {
                    uri.toString()
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val contentUri: Uri = when (split[0]) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> return uri.toString()
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            } else {
                uri.toString()
            }
        } else if ("content".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) {
            getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) {
            uri.path ?: uri.toString()
        } else {
            uri.toString()
        }
    }

    private fun getDataColumn(
            context: Context,
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
    ): String {
        context.contentResolver.query(
            uri,
            arrayOf("_data"),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow("_data")
                return cursor.getString(columnIndex)
            }
        }
        return uri.toString()
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getLicenseFileContent(context: Context): File? {
        val storageDir = File(
            context.filesDir,
            "licenses"
        )
        if (storageDir.exists()) {
            val licenseFile = File(
                storageDir,
                "license"
            )
            if (licenseFile.exists()) {
                return licenseFile
            }
        }/* val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
         val licensesFolder = File(
             downloadDirectory,
             "${context.packageName}/licenses"
         )
         val licenseFile = File(
             licensesFolder,
             "license"
         )

         if (licenseFile.exists()) {
             return licenseFile
         }*/
        return null
    }

    fun saveRtaLicense(
            context: Context,
            licenseStr: String
    ) {
        val storageDir = File(
            context.filesDir,
            "licenses"
        )
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val destinationFile = File(
            storageDir,
            "license"
        )

        try {
            // Use OutputStreamWriter to write the data to the file
            FileOutputStream(destinationFile).use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(licenseStr)
                }
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHtmlFile(
            context: Context,
            urlSuffix: String
    ): String {
        val rootDir = File(
            context.filesDir,
            "Reports"
        )
        if (!rootDir.exists()) {
            return ""
        }

        val file = File(
            rootDir,
            urlSuffix
        )

        if (!file.exists()) {
            return ""
        }
        return getFileContent(
            context,
            Uri.fromFile(file)
        )
    }

    fun getBitmapFromPath(
            context: Context,
            path: Uri
    ): Bitmap? {
        val image = getFileFromUri(
            context,
            path
        )
        try {
            val bmOptions = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeFile(
                image?.absolutePath,
                bmOptions
            )
            if (bitmap == null) {
                bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    path
                )
            }
            return Bitmap.createScaledBitmap(
                bitmap!!,
                100,
                50,
                true
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getFileModels(
            directoryFile: File,
            selectedPaySlip: String?,
            selectedPayTicket: String?
    ): MutableList<FileModel> {
        val result = mutableListOf<FileModel>()

        if (directoryFile.exists() && directoryFile.isDirectory) {
            directoryFile.listFiles()?.forEach { file ->
                // If the current file is a directory, recursively gather its children
                if (file.isDirectory) {
                    result.addAll(
                        getFileModels(
                            file,
                            selectedPaySlip,
                            selectedPayTicket
                        )
                    )
                } else {
                    val fileName = file.name
                    val directoryName = directoryFile.name
                    val isPaySlip = fileName.contains(
                        "payslip",
                        ignoreCase = true
                    )
                    val selected = if (isPaySlip) selectedPaySlip?.equals("$directoryName/$fileName") == true
                    else selectedPayTicket?.equals("$directoryName/$fileName") == true
                    val fileModel = FileModel(
                        fileName = fileName,
                        parentName = directoryName,
                        isPaySlip = isPaySlip,
                        selected = selected
                    )
                    result.add(fileModel)
                }
            }
        }

        return result
    }

}