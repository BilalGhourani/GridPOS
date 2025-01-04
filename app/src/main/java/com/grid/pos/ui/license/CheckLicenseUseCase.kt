package com.grid.pos.ui.license

import android.content.Context
import androidx.core.net.toUri
import com.grid.pos.App
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Constants
import com.grid.pos.utils.CryptoUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import java.io.File
import java.util.Date

class CheckLicenseUseCase(private val companyRepository: CompanyRepository,
        private val invoiceHeaderRepository: InvoiceHeaderRepository) {

    suspend fun invoke(context: Context,
            onResult: (Int, String) -> Unit) {// if license not exist add new screen to take the file
        try {
            val licenseFile = FileUtils.getLicenseFileContent(context)
            if (licenseFile != null) {
                val isLicensed = checkLicenseFile(context, licenseFile)
                if (isLicensed) {//check the content
                    val fileContent = FileUtils.getFileContent(context, licenseFile.toUri())
                    val decryptContent = CryptoUtils.decrypt(fileContent,
                        App.getInstance().getConfigValue("key_for_license"))
                    val sep = "\\$@$\\"
                    val segs = decryptContent.split(sep)
                    val segsSize = segs.size
                    val devID = if (segsSize > 0) segs[0] else ""
                    val expiryDateStr = if (segsSize > 1) segs[1] else ""
                    val isReadyToActivate = ((if (segsSize > 2) segs[2] else "0").toIntOrNull() ?: 0) == 1
                    val daysNumber = (if (segsSize > 3) segs[3] else "7").toIntOrNull() ?: 0
                    val sameDeviceID = devID.equals(Utils.getDeviceID(context), ignoreCase = true)
                    val expiryDate = DateHelper.getDateFromString(expiryDateStr, "yyyyMMdd")
                    val currDate = Date()
                    if (sameDeviceID) {
                        if (currDate.time < expiryDate.time) {
                            if (isReadyToActivate && daysNumber > 0) {
                                //would you like to activate
                                val newDate = DateHelper.addDays(currDate, daysNumber)
                                val newExpiryDate = DateHelper.getDateInFormat(newDate, "yyyyMMdd")
                                val newContent = "$devID$sep$newExpiryDate"
                                val encryptedContent = CryptoUtils.encrypt(newContent,
                                    App.getInstance().getConfigValue("key_for_license"))
                                FileUtils.saveRtaLicense(context, encryptedContent)
                            }
                            onResult.invoke(Constants.SUCCEEDED, "")
                        } else {
                            onResult.invoke(Constants.LICENSE_EXPIRED,
                                "Your license has expired. Please renew it to continue.")
                        }
                    } else {
                        onResult.invoke(Constants.WRONG_DEVICE_ID,
                            "This license is not valid for this device. Contact support.")
                    }
                } else {
                    onResult.invoke(Constants.WRONG_DEVICE_DATE,
                        "Incorrect device date detected. Please check your device settings.")
                }
            } else {
                onResult.invoke(Constants.LICENSE_NOT_FOUND,
                    "License not found. Please ensure your app is registered.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult.invoke(Constants.LICENSE_ACCESS_DENIED,
                e.message ?: "An error occurred while verifying your license. Please try again or contact support if the issue persists.")
        }
    }

    private suspend fun checkLicenseFile(context: Context, licenseFile: File): Boolean {
        val currentDate = DateHelper.editDate(Date(), 0, 0, 0)
        val firstInstallTime = Utils.getFirstInstallationTime(context)
        val firstInstallDate = DateHelper.editDate(Date(firstInstallTime), 0, 0, 0)

        val licCreatedDate = DateHelper.editDate(Date(licenseFile.lastModified()), 0, 0, 0)
        if (DateHelper.getDaysDiff(currentDate, licCreatedDate) < 0 || DateHelper.getDaysDiff(
                firstInstallDate, currentDate) < 0 || DateHelper.getDaysDiff(firstInstallDate,
                licCreatedDate) < 0
        ) {
            companyRepository.disableCompanies(true)
            //db.execSQL("UPDATE company SET cmp_ss=1")
            // Optionally, you might want to shut down the app or handle it appropriately
            return false
        } else {
            val company = companyRepository.getCompanyById(SettingsModel.getCompanyID() ?: "")
            if (company?.companySS == true) {
                val lastInvoice = invoiceHeaderRepository.getLastInvoice()
                val lastInvoiceDate = lastInvoice?.invoiceHeadTimeStamp ?: lastInvoice?.invoiceHeadDateTime?.let {
                    Date(it)
                }
                return if (currentDate.time >= (lastInvoiceDate?.time ?: 0) || licCreatedDate.time >= currentDate.time) {
                    companyRepository.disableCompanies(false)
                    //db.execSQL("UPDATE company SET cmp_ss=0")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    true
                } else {
                    companyRepository.disableCompanies(true)
                    //db.execSQL("UPDATE company SET cmp_ss=1")
                    // Optionally, you might want to shut down the app or handle it appropriately
                    false
                }
            }
            return true
        }
    }
}