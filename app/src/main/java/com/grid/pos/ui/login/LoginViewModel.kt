package com.grid.pos.ui.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.SharedViewModel
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun login(context: Context) {
        val username = usernameState.value.trim()
        val password = passwordState.value.trim()
        if (username.isEmpty() || password.isEmpty()) {
            showWarning("Please fill all inputs")
            return
        }
        if (App.getInstance().isMissingFirebaseConnection()) {
            showWarning("unable to connect to server", "Settings") {
                navigateTo("SettingsView")
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
                            loginNow(username, password)
                        }

                        else -> {
                            showWarning(message)
                            navigateTo("LicenseView")
                        }
                    }
                }
            })
        }
    }

    private suspend fun loginNow(username: String, password: String) {
        withContext(Dispatchers.IO) {
            val companyId = SettingsModel.getCompanyID()
            if (!companyId.isNullOrEmpty()) {
                SettingsModel.currentCompany = companyRepository.getCompanyById(companyId ?: "")
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
                proceedWithLogin()
            } ?: run {
                if (!loginResponse.error.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning(loginResponse.error, "Settings") {
                            navigateTo("SettingsView")
                        }
                    }
                } else if (loginResponse.allUsersSize == 0) {
                    if (SettingsModel.isConnectedToSqlite() || SettingsModel.isConnectedToFireStore()) {
                        val companies = companyRepository.getAllCompanies()
                        withContext(Dispatchers.Main) {
                            sharedViewModel.isRegistering = true
                            showLoading(false)
                            if (companies.isEmpty()) {
                                showWarning(
                                    "No companies found!, do you want to register?",
                                    "Register"
                                ) {
                                    navigateTo("ManageCompaniesView")
                                }
                            } else if (SettingsModel.getCompanyID().isNullOrEmpty()) {
                                showWarning("select your current company to proceed!", "Settings") {
                                    navigateTo("SettingsView")
                                }
                            } else {
                                showWarning(
                                    "No users found!, do you want to create a user?",
                                    "Create"
                                ) {
                                    navigateTo("ManageUsersView")
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

    private suspend fun proceedWithLogin() {
        sharedViewModel.isLoggedIn = true
        sharedViewModel.homeWarning = null
        sharedViewModel.initiateValues()
        withContext(Dispatchers.Main) {
            usernameState.value = ""
            passwordState.value = ""
            showLoading(false)
            SettingsModel.currentUser?.let {
                if (it.userPosMode && it.userTableMode) {
                    navigateTo("HomeView")
                } else if (it.userPosMode) {
                    navigateTo("POSView")
                } else if (it.userTableMode) {
                    navigateTo("TablesView")
                } else {
                    navigateTo("HomeView")
                }
            }
        }
    }
}