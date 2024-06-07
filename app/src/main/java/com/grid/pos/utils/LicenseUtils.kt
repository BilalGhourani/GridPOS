package com.grid.pos.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.grid.pos.data.Company.Company
import com.grid.pos.model.SettingsModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LicenseUtils {

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun getFirstInstallationTime(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            packageInfo.firstInstallTime
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            0
        }
    }

    fun formatDate(date: Date): String {
        return DateHelper.getDateInFormat(
            date,
            "dd-MMM-yy"
        )
    }

    private fun getLastWriteTime(context: Context): Date {
        return FileUtils.getLastWriteTimeFromUri(
            context,
            Uri.parse(SettingsModel.licenseFilePath)
        ) ?: Date()
    }

    fun checkLicense(
            context: Context,
            currentCompany: Company,
            lastInvoiceDate: Date,
            onUpdateCompany: (Boolean, String, String) -> Unit
    ) {
        val currentDate = Date()
        val firstInstallTime = getFirstInstallationTime(context)
        val firstInstallDate = Date(firstInstallTime)

        val licCreatedDate = getLastWriteTime(context)
        if (DateHelper.getDatesDiff(
                currentDate,
                licCreatedDate
            ) < 0 || DateHelper.getDatesDiff(
                firstInstallDate,
                currentDate
            ) < 0 || DateHelper.getDatesDiff(
                firstInstallDate,
                licCreatedDate
            ) < 0
        ) {
            onUpdateCompany.invoke(
                true,
                "CheckLicense",
                "Local server is not responding, contact your system administrator"
            )
            //db.execSQL("UPDATE company SET cmp_ss=1")
            // Optionally, you might want to shut down the app or handle it appropriately
            return
        } else {
            if (currentCompany.companySS) {
                if (currentDate >= lastInvoiceDate || licCreatedDate >= currentDate) {
                    onUpdateCompany.invoke(
                        false,
                        "",
                        ""
                    )
                    //db.execSQL("UPDATE company SET cmp_ss=0")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    return
                } else {
                    onUpdateCompany.invoke(
                        true,
                        "CheckLicense",
                        "Local server is not responding, contact your system administrator"
                    )
                    //db.execSQL("UPDATE company SET cmp_ss=1")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    return
                }
            }
        }
    }

}