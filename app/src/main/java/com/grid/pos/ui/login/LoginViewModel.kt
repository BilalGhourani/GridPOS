package com.grid.pos.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.User.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Constants
import com.grid.pos.utils.CryptoUtils
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow(LoginState())
    val usersState: MutableStateFlow<LoginState> = _usersState

    fun login(
        context: Context,
        username: String,
        password: String
    ) {
        if (true) {
            checkLicense(context)
            return
        }
        if (username.isEmpty() || password.isEmpty()) {
            usersState.value = usersState.value.copy(
                warning = Event("Please fill all inputs"),
                isLoading = false,
                warningAction = ""
            )
            return
        }
        if (App.getInstance().isMissingFirebaseConnection()) {
            usersState.value = usersState.value.copy(
                warning = Event("unable to connect to server"),
                isLoading = false,
                warningAction = "Settings"
            )
            return
        }
        if (SettingsModel.currentCompany?.companySS == true) {
            viewModelScope.launch(Dispatchers.Main) {
                usersState.value = usersState.value.copy(
                    warning = Event(SettingsModel.companyAccessWarning),
                    isLoading = false,
                    warningAction = ""
                )
            }
            return
        }
        usersState.value = usersState.value.copy(
            isLoading = true,
            warningAction = ""
        )
        viewModelScope.launch(Dispatchers.IO) {
            val loginResponse: LoginResponse = repository.getUserByCredentials(username, password)
            loginResponse.user?.let {
                SettingsModel.currentUserId = it.userId
                SettingsModel.currentUser = it
                DataStoreManager.putString(
                    DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key,
                    it.userId
                )
                viewModelScope.launch(Dispatchers.Main) {
                    usersState.value = usersState.value.copy(
                        selectedUser = it, isLoading = false,
                        isLoggedIn = true
                    )
                }
            } ?: run {
                if (loginResponse.allUsersSize == 0) {
                    if (SettingsModel.isConnectedToSqlite()) {
                        val companies = companyRepository.getAllCompanies()
                        viewModelScope.launch(Dispatchers.Main) {
                            if (companies.isEmpty()) {
                                usersState.value = usersState.value.copy(
                                    warning = Event("no company found!"),
                                    isLoading = false,
                                    warningAction = "Create a Company"
                                )
                            } else if (SettingsModel.localCompanyID.isNullOrEmpty()) {
                                usersState.value = usersState.value.copy(
                                    warning = Event("select a company to proceed!"),
                                    isLoading = false,
                                    warningAction = "Settings"
                                )
                            } else {
                                usersState.value = usersState.value.copy(
                                    isLoading = false,
                                    warning = Event("no user found!"),
                                    warningAction = "Register"
                                )
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            usersState.value = usersState.value.copy(
                                isLoading = false,
                                warning = Event("no user found!"),
                                warningAction = ""
                            )
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        usersState.value = usersState.value.copy(
                            isLoading = false,
                            warning = Event("Username or Password are incorrect!"),
                            warningAction = ""
                        )
                    }
                }
            }

        }
    }

    private fun checkLicense(context: Context) :Boolean {// if license not exist add new screen to take the file
        val licenseFile = FileUtils.getLicenseFileContent(context)
        if (licenseFile != null) {
            val fileContent = licenseFile.readText()
            val decContent = CryptoUtils.decrypt(
                fileContent,
                App.getInstance().getConfigValue("key_for_license")
            )
            return checkLicense(context, licenseFile, Company(), Date())
        } else {
            return false
            //FileUtils.saveLicenseContent(context, Constants.LICENSE_FILE_CONTENT)
        }
    }

    private fun checkLicense(
        context: Context,
        licenseFile: File,
        currentCompany: Company,
        lastInvoiceDate: Date
    ): Boolean {
        val currentDate = Date()
        val firstInstallTime = Utils.getFirstInstallationTime(context)
        val firstInstallDate = Date(firstInstallTime)

        val licCreatedDate = Date(licenseFile.lastModified())
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
            if (currentCompany.companySS) {
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