package com.grid.pos.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grid.pos.data.DataModel
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.ConnectionModel
import com.grid.pos.model.HomeSectionModel
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone
import java.util.UUID

object Utils {

    val connections = mutableListOf<DataModel>(
        ConnectionModel(CONNECTION_TYPE.LOCAL.key),
        ConnectionModel(CONNECTION_TYPE.FIRESTORE.key),
        ConnectionModel(CONNECTION_TYPE.SQL_SERVER.key)
    )

    fun getHomeList(): MutableList<HomeSectionModel> {
        return mutableListOf(
            HomeSectionModel(
                "Currency",
                "ManageCurrenciesView"
            ),
            HomeSectionModel(
                "Company",
                "ManageCompaniesView"
            ),
            HomeSectionModel(
                "User",
                "ManageUsersView"
            ),
            HomeSectionModel(
                "Third Party",
                "ManageThirdPartiesView"
            ),
            HomeSectionModel(
                "Family",
                "ManageFamiliesView"
            ),
            HomeSectionModel(
                "Item",
                "ManageItemsView"
            ),
            HomeSectionModel(
                "Printer",
                "POSPrinterView"
            ),
            HomeSectionModel(
                "Reports",
                "ReportsView"
            ),
            HomeSectionModel(
                "POS",
                "POSView"
            ),
            HomeSectionModel(
                "Table",
                "TablesView"
            )
        )
    }

    fun generateRandomUuidString(): String {
        if (!SettingsModel.isSqlServerWebDb && SettingsModel.connectionType == CONNECTION_TYPE.SQL_SERVER.key) {
            val random = Random()
            return (BigInteger(24, random)).toString()
        }
        return UUID.randomUUID().toString()
    }

    fun floatToColor(
            hue: Float,
            saturation: Float = 1f,
            brightness: Float = 1f
    ): Color {
        // Convert HSV to RGB
        val hsv = floatArrayOf(
            hue,
            saturation,
            brightness
        )
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    fun getDoubleValue(
            new: String,
            old: String
    ): String {
        return if (new.isEmpty()) {
            new
        } else {
            when (new.toDoubleOrNull()) {
                null -> old //old value
                else -> new   //new value
            }
        }
    }

    fun getIntValue(
            new: String,
            old: String
    ): String {
        return if (new.isEmpty()) {
            new
        } else {
            when (new.toIntOrNull()) {
                null -> old //old value
                else -> new  //new value
            }
        }
    }

    fun getItemsNumberStr(
            items: MutableList<InvoiceItemModel>
    ): String {
        val size = items.size
        return if (size <= 1) {
            "$size item"
        } else {
            "$size items"
        }
    }

    fun isTablet(configuration: Configuration): Boolean {
        return configuration.screenWidthDp > 840
    }

    fun getListHeight(
            listSize: Int = 0,
            cellHeight: Int,
            min: Int = 1,
            max: Int = 8
    ): Dp {
        var size = listSize
        if (size < min) size = min
        else if (size > max) size = max
        return (size * cellHeight).dp + 50.dp
    }

    fun getCurrentYear(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currentYear = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Year.now().value
        } else {
            calendar[Calendar.YEAR]
        }
        return currentYear.toString()
    }

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

}