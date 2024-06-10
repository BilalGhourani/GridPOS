package com.grid.pos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.User.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow(LoginState())
    val usersState: MutableStateFlow<LoginState> = _usersState

    fun login(
        username: String,
        password: String
    ) {
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
}