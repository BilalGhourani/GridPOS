package com.grid.pos.ui.license

import android.content.Context
import androidx.core.net.toUri
import com.grid.pos.App
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Constants
import com.grid.pos.utils.CryptoUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class CheckLicenseUseCase(
        private val companyRepository: CompanyRepository,
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) {

    suspend fun invoke(
            context: Context,
            onResult: (Int) -> Unit
    ) {// if license not exist add new screen to take the file
        val companyID = SettingsModel.getCompanyID()
        if (!companyID.isNullOrEmpty()) {
            try {
                val licenseFile = FileUtils.getLicenseFileContent(context)
                if (licenseFile != null) {
                    val company = companyRepository.getCompanyById(companyID)
                    val lastInvoice = invoiceHeaderRepository.getLastInvoice()
                    val lastInvoiceDate = lastInvoice?.invoiceHeadTimeStamp ?: lastInvoice?.invoiceHeadDateTime?.let { Date(it) }
                    val isLicensed = checkLicenseFile(
                        context,
                        licenseFile,
                        company,
                        lastInvoiceDate
                    )
                    if (isLicensed) {//check the content
                        val fileContent = FileUtils.getFileContent(
                            context,
                            licenseFile.toUri()
                        )
                        val decryptContent = CryptoUtils.decrypt(
                            fileContent,
                            App.getInstance().getConfigValue("key_for_license")
                        )
                        val sep = "\\$@$\\"
                        val segs = decryptContent.split(sep)
                        val segsSize = segs.size
                        val devID = if (segsSize > 0) segs[0] else ""
                        val expiryDateStr = if (segsSize > 1) segs[1] else ""
                        val isReadyToActivate = ((if (segsSize > 2) segs[2] else "0").toIntOrNull() ?: 0) == 1
                        val daysNumber = (if (segsSize > 3) segs[3] else "7").toIntOrNull() ?: 0
                        val sameDeviceID = devID.equals(
                            Utils.getDeviceID(context),
                            ignoreCase = true
                        )
                        val expiryDate = DateHelper.getDateFromString(expiryDateStr, "yyyyMMdd")
                        val currDate = Date()
                        if (sameDeviceID && currDate.time < expiryDate.time && !isReadyToActivate
                        ) {
                            onResult.invoke(Constants.SUCCEEDED)
                        } else if (currDate.time < expiryDate.time && isReadyToActivate) {
                            //would you like to activate
                            val newDate = DateHelper.addDays(
                                currDate,
                                daysNumber
                            )
                            val newExpiryDate = DateHelper.getDateInFormat(
                                newDate,
                                "yyyyMMdd"
                            )
                            val newContent = "$devID$sep$newExpiryDate"
                            FileUtils.saveRtaLicense(context,newContent)
                            onResult.invoke(Constants.SUCCEEDED)
                        } else {
                            onResult.invoke(Constants.LICENSE_EXPIRED)
                        }
                    } else {
                        onResult.invoke(Constants.LICENSE_EXPIRED)
                    }
                } else {
                    onResult.invoke(Constants.LICENSE_NOT_FOUND)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult.invoke(Constants.LICENSE_ACCESS_DENIED)
            }
        }
    }

    private fun checkLicenseFile(
            context: Context,
            licenseFile: File,
            currentCompany: Company?,
            lastInvoiceDate: Date?
    ): Boolean {
        val currentDate = DateHelper.editDate(
            Date(),
            0,
            0,
            0
        )
        val firstInstallTime = Utils.getFirstInstallationTime(context)
        val firstInstallDate = DateHelper.editDate(
            Date(firstInstallTime),
            0,
            0,
            0
        )

        val licCreatedDate = DateHelper.editDate(
            Date(licenseFile.lastModified()),
            0,
            0,
            0
        )
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
            CoroutineScope(Dispatchers.IO).launch {
                companyRepository.disableCompanies(true)
            }
            //db.execSQL("UPDATE company SET cmp_ss=1")
            // Optionally, you might want to shut down the app or handle it appropriately
            return false
        } else {
            if (currentCompany?.companySS == true) {
                if (currentDate >= lastInvoiceDate || licCreatedDate >= currentDate) {
                    CoroutineScope(Dispatchers.IO).launch {
                        companyRepository.disableCompanies(false)
                    }
                    //db.execSQL("UPDATE company SET cmp_ss=0")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    return true
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        companyRepository.disableCompanies(true)
                    }
                    //db.execSQL("UPDATE company SET cmp_ss=1")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    return false
                }
            }
            return true
        }
    }
}