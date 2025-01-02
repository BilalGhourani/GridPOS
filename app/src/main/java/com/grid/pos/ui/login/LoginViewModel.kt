package com.grid.pos.ui.login

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.ui.license.CheckLicenseUseCase
import com.grid.pos.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
        private val checkLicenseUseCase: CheckLicenseUseCase,
        private val repository: UserRepository,
        private val companyRepository: CompanyRepository
) : BaseViewModel() {

    private val _usersState = MutableStateFlow(LoginState())
    val usersState: MutableStateFlow<LoginState> = _usersState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun login(
            context: Context,
            username: String,
            password: String
    ) {
        usersState.value = usersState.value.copy(
            isLoading = true,
            needRegistration = false,
            warning = null,
            warningAction = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            //CryptoUtils.test(App.getInstance().getConfigValue("key_for_license"))
            checkLicenseUseCase.invoke(context,
                onResult = { result ->
                    viewModelScope.launch(Dispatchers.Main) {
                        when (result) {
                            Constants.SUCCEEDED -> {
                                loginNow(
                                    username,
                                    password
                                )
                            }

                            Constants.LICENSE_NOT_FOUND, Constants.LICENSE_EXPIRED, Constants.LICENSE_ACCESS_DENIED -> {
                                usersState.value = usersState.value.copy(
                                    needLicense = true,
                                    isLoading = false,
                                    warning = null,
                                    warningAction = null
                                )
                            }
                        }
                    }
                })
        }
    }

    private fun loginNow(
            username: String,
            password: String
    ) {
        if (username.isEmpty() || password.isEmpty()) {
            usersState.value = usersState.value.copy(
                warning = Event("Please fill all inputs"),
                isLoading = false,
                needRegistration = false,
                warningAction = null
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
                    needRegistration = false,
                    warningAction = null
                )
            }
            return
        }
        usersState.value = usersState.value.copy(
            isLoading = true,
            needRegistration = false,
            warning = null,
            warningAction = null
        )
        viewModelScope.launch(Dispatchers.IO) {
            val loginResponse: LoginResponse = repository.getUserByCredentials(
                username,
                password
            )
            loginResponse.user?.let {
                SettingsModel.currentUser = it
                viewModelScope.launch(Dispatchers.Main) {
                    usersState.value = usersState.value.copy(
                        selectedUser = it,
                        needRegistration = false,
                        isLoggedIn = true
                    )
                }
            } ?: run {
                if (!loginResponse.error.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        usersState.value = usersState.value.copy(
                            warning = Event(loginResponse.error),
                            isLoading = false,
                            needRegistration = false,
                            warningAction = "Settings"
                        )
                    }
                } else if (loginResponse.allUsersSize == 0) {
                    if (SettingsModel.isConnectedToSqlite() || SettingsModel.isConnectedToFireStore()) {
                        val companies = companyRepository.getAllCompanies()
                        withContext(Dispatchers.Main) {
                            if (companies.isEmpty()) {
                                usersState.value = usersState.value.copy(
                                    warning = Event("No companies found!, do you want to register?"),
                                    isLoading = false,
                                    needRegistration = true,
                                    warningAction = "Register"
                                )
                            } else if (SettingsModel.localCompanyID.isNullOrEmpty()) {
                                usersState.value = usersState.value.copy(
                                    warning = Event("select your current company to proceed!"),
                                    isLoading = false,
                                    needRegistration = true,
                                    warningAction = "Settings"
                                )
                            } else {
                                usersState.value = usersState.value.copy(
                                    isLoading = false,
                                    needRegistration = true,
                                    warning = Event("No users found!, do you want to create a user?"),
                                    warningAction = "Create"
                                )
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            usersState.value = usersState.value.copy(
                                isLoading = false,
                                warning = Event("No users found!"),
                                warningAction = null
                            )
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        usersState.value = usersState.value.copy(
                            isLoading = false,
                            warning = Event("Username or Password are incorrect!"),
                            warningAction = null
                        )
                    }
                }
            }

        }
    }
}