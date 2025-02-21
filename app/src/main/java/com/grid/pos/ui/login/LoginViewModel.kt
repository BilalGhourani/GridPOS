package com.grid.pos.ui.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.SharedViewModel
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.ui.license.CheckLicenseUseCase
import com.grid.pos.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val checkLicenseUseCase: CheckLicenseUseCase,
    private val repository: UserRepository,
    private val companyRepository: CompanyRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    var usernameState = mutableStateOf("")
    var passwordState = mutableStateOf("")
    var companies = mutableListOf<Company>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun login(context: Context, callback: (String) -> Unit) {
        val username = usernameState.value.trim()
        val password = passwordState.value.trim()
        if (username.isEmpty() || password.isEmpty()) {
            showWarning("Please fill all inputs")
            return
        }
        if (App.getInstance().isMissingFirebaseConnection()) {
            showWarning("unable to connect to server", "Settings") {
                callback.invoke("SettingsView")
            }
            return
        }
        showLoading(true)

        viewModelScope.launch(Dispatchers.IO) {
            //CryptoUtils.test(App.getInstance().getConfigValue("key_for_license"))
            checkLicenseUseCase.invoke(context, onResult = { result, message ->
                viewModelScope.launch(Dispatchers.Main) {
                    when (result) {
                        Constants.SUCCEEDED -> {
                            loginNow(username, password, callback)
                        }

                        else -> {
                            showWarning(message)
                            callback.invoke("LicenseView")
                        }
                    }
                }
            })
        }
    }

    private suspend fun loginNow(username: String, password: String, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val companyId = SettingsModel.getCompanyID()
            if (!companyId.isNullOrEmpty()) {
                SettingsModel.currentCompany = companyRepository.getCompanyById(companyId)
                if (SettingsModel.currentCompany?.companySS == true) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning(SettingsModel.companyAccessWarning)
                    }
                    return@withContext
                }
            }
            val loginResponse: LoginResponse = repository.getUserByCredentials(username, password)
            loginResponse.user?.let {
                SettingsModel.currentUser = it
                proceedWithLogin(callback)
            } ?: run {
                if (!loginResponse.error.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning(loginResponse.error, "Settings") {
                            callback.invoke("SettingsView")
                        }
                    }
                } else if (loginResponse.allUsersSize == 0) {
                    if (SettingsModel.isConnectedToSqlite() || SettingsModel.isConnectedToFireStore()) {
                        if (companies.isEmpty()) {
                            companies = companyRepository.getAllCompanies()
                        }
                        withContext(Dispatchers.Main) {
                            sharedViewModel.isRegistering = true
                            showLoading(false)
                            if (companies.isEmpty()) {
                                showWarning(
                                    "No companies found!, do you want to register?",
                                    "Register"
                                ) {
                                    callback.invoke("ManageCompaniesView")
                                }
                            } else if (SettingsModel.getCompanyID().isNullOrEmpty()) {
                                showWarning("select your current company to proceed!", "Settings") {
                                    callback.invoke("SettingsView")
                                }
                            } else {
                                showWarning(
                                    "No users found!, do you want to create a user?",
                                    "Create"
                                ) {
                                    callback.invoke("ManageUsersView")
                                }
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            showLoading(false)
                            showWarning("No users found!")
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        showLoading(false)
                        showWarning("Username or Password are incorrect!")
                    }
                }
            }

        }
    }

    fun backPressed() {
        sharedViewModel.finish()
    }

    private suspend fun proceedWithLogin(callback: (String) -> Unit) {
        sharedViewModel.isLoggedIn = true
        sharedViewModel.homeWarning = null
        sharedViewModel.initiateValues()
        withContext(Dispatchers.Main) {
            usernameState.value = ""
            passwordState.value = ""
            showLoading(false)
            SettingsModel.currentUser?.let {
                if (sharedViewModel.checkPermission("Run In POS Mode", false)) {
                    callback.invoke("TablesView")
                } else if (it.userPosMode && it.userTableMode) {
                    callback.invoke("HomeView")
                } else if (it.userPosMode) {
                    callback.invoke("POSView")
                } else if (it.userTableMode) {
                    callback.invoke("TablesView")
                } else {
                    callback.invoke("HomeView")
                }
            }
        }
    }
}