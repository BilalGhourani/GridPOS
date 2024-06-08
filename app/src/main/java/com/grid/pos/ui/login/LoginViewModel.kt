package com.grid.pos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.encryptCBC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
        val decPassword = password.encryptCBC()
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllUsers()
            addAdministratorIfNeeded(
                username,
                result
            )
            val size = result.size
            if (size == 0) {
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
                var user: User? = null
                result.forEach {
                    if (username.equals(
                            it.userUsername,
                            ignoreCase = true
                        ) && decPassword.equals(
                            it.userPassword,
                            ignoreCase = true
                        )
                    ) {
                        user = it
                        SettingsModel.currentUserId = it.userId
                        SettingsModel.currentUser = it
                        viewModelScope.launch(Dispatchers.IO) {
                            DataStoreManager.putString(
                                DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key,
                                it.userId
                            )
                        }
                        return@forEach
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    if (user == null) {
                        usersState.value = usersState.value.copy(
                            isLoading = false,
                            warning = Event("Username or Password are incorrect!"),
                            warningAction = ""
                        )
                    } else {
                        usersState.value = usersState.value.copy(
                            selectedUser = user!!,/*isLoading = false,*/
                            isLoggedIn = true
                        )
                    }
                }
            }

            /* val user =  repository.getUserByCredentials(username, password)
              user?.let {
                  viewModelScope.launch(Dispatchers.Main) {
                      usersState.value = usersState.value.copy(
                          selectedUser = it,
                          isLoading = false,
                          isLoggedIn = true
                      )
                  }
              }?:run {
                  viewModelScope.launch(Dispatchers.Main) {
                      usersState.value = usersState.value.copy(
                          isLoading = false,
                          warning = Event("no user fount"),
                          warningAction = "Register"
                      )
                  }
              }*/
        }
    }

    private fun addAdministratorIfNeeded(
            username: String,
            users: MutableList<User>
    ) {
        if (users.size == 0 && username.equals(
                "administrator",
                ignoreCase = true
            )
        ) {
            users.add(
                User(
                    "administrator",
                    null,
                    "Administrator",
                    "administrator",
                    DateHelper.getDateInFormat(
                        Date(),
                        "dd-MMM-yyyy"
                    ).encryptCBC()
                )
            )
        }
    }
}