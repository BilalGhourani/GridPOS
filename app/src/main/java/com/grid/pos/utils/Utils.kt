package com.grid.pos.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grid.pos.BuildConfig
import com.grid.pos.R
import com.grid.pos.data.DataModel
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.ConnectionModel
import com.grid.pos.model.Country
import com.grid.pos.model.HomeSectionModel
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.Language
import com.grid.pos.model.ORIENTATION_TYPE
import com.grid.pos.model.OrientationModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.ReportLanguage
import com.grid.pos.model.SettingsModel
import java.math.BigInteger
import java.time.Year
import java.util.Calendar
import java.util.Random
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

object Utils {

    val connections = mutableListOf<DataModel>(
        ConnectionModel(CONNECTION_TYPE.LOCAL.key),
        ConnectionModel(CONNECTION_TYPE.FIRESTORE.key),
        ConnectionModel(CONNECTION_TYPE.SQL_SERVER.key)
    )

    val orientations = mutableListOf<DataModel>(
        OrientationModel(ORIENTATION_TYPE.PORTRAIT.key),
        OrientationModel(ORIENTATION_TYPE.LANDSCAPE.key),
        OrientationModel(ORIENTATION_TYPE.DEVICE_SENSOR.key)
    )

    var isTablet: Boolean? = null;
    var isDeviceLargerThan7Inches: Boolean? = null;

    fun getColumnCount(context: Context): Int {
       return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            3
        } else {
            5
        }
    }

    fun randomColor(): Color {
        val red = kotlin.random.Random.nextFloat()
        val green = kotlin.random.Random.nextFloat()
        val blue = kotlin.random.Random.nextFloat()
        return Color(red, green, blue)
    }

    fun getHomeList(): MutableList<HomeSectionModel> {
        if (!BuildConfig.DEBUG && SettingsModel.isConnectedToSqlServer()) {
            return mutableListOf(
                HomeSectionModel(
                    R.drawable.pos,
                    "POS",
                    "POSView"
                ),
                HomeSectionModel(
                    R.drawable.tables,
                    "Table",
                    "TablesView"
                )
            )
        }
        return mutableListOf(
            HomeSectionModel(
                R.drawable.companies,
                "Company",
                "ManageCompaniesView"
            ),
            HomeSectionModel(
                R.drawable.currencies,
                "Currency",
                "ManageCurrenciesView"
            ),
            HomeSectionModel(
                R.drawable.users,
                "User",
                "ManageUsersView"
            ),
            HomeSectionModel(
                R.drawable.third_parties,
                "Third Party",
                "ManageThirdPartiesView"
            ),
            HomeSectionModel(
                R.drawable.families,
                "Family",
                "ManageFamiliesView"
            ),
            HomeSectionModel(
                R.drawable.items,
                "Item",
                "ManageItemsView"
            ),
            HomeSectionModel(
                R.drawable.printer,
                "Printer",
                "POSPrinterView"
            ),
            HomeSectionModel(
                R.drawable.pos,
                "POS",
                "POSView"
            ),
            HomeSectionModel(
                R.drawable.tables,
                "Table",
                "TablesView"
            ),
            HomeSectionModel(
                R.drawable.sales_reports,
                "Sales Reports",
                "ReportsView"
            ),
            HomeSectionModel(
                R.drawable.setup_reports,
                "Setup Reports",
                "ReportsListView"
            ),
            HomeSectionModel(
                R.drawable.backup_restore,
                "Backup & Restore",
                "BackupView"
            )
        )
    }

    fun getReportLanguages(withDefault: Boolean): MutableList<ReportLanguage> {
        val result = mutableListOf<ReportLanguage>()
        Language.entries.forEach {
            result.add(ReportLanguage(it))
        }
        if (!withDefault) {
            result.removeAt(0)
        }
        return result
    }

    fun getReportCountries(withDefault: Boolean): MutableList<ReportCountry> {
        val result = mutableListOf<ReportCountry>()
        Country.entries.forEach {
            result.add(
                ReportCountry(
                    it.code,
                    it.value
                )
            )
        }
        if (!withDefault) {
            result.removeAt(0)
        }
        return result
    }

    fun generateRandomUuidString(): String {
        if (!SettingsModel.isSqlServerWebDb && SettingsModel.connectionType == CONNECTION_TYPE.SQL_SERVER.key) {
            val random = Random()
            return (BigInteger(
                24,
                random
            )).toString()
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

    fun convertColorToInt(
            color: Color
    ): Int {
        return android.graphics.Color.rgb(
            color.red,
            color.green,
            color.blue
        )
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

    fun roundDoubleValue(
            value: Double,
            decimal: Int? = 2
    ): Double {
        val doubleStr = String.format(
            "%.${decimal}f",
            value
        )
        return doubleStr.toDoubleOrNull() ?: value
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

    fun getDoubleOrZero(
            value: Double?
    ): Double {
        return if (value?.isNaN() == true) {
            0.0
        } else {
            value ?: 0.0
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
        if (isTablet != null) {
            return isTablet!!
        }
        isTablet = configuration.smallestScreenWidthDp >= 600
        return isTablet!!
    }

    fun isDeviceLargerThan7Inches(
            context: Context
    ): Boolean {
        if (isDeviceLargerThan7Inches != null) {
            return isDeviceLargerThan7Inches!!
        }
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics

        // Calculate the screen width and height in inches
        val screenWidthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val screenHeightInches = displayMetrics.heightPixels / displayMetrics.ydpi

        // Calculate the diagonal screen size in inches
        val screenSizeInches = sqrt(screenWidthInches.pow(2) + screenHeightInches.pow(2))

        // Check if the screen size is greater than or equal to 7 inches
        isDeviceLargerThan7Inches = screenSizeInches >= 7.0
        return isDeviceLargerThan7Inches!!
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