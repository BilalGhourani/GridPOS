package com.grid.pos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
        private val repository: UserRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow(LoginState())
    val usersState: MutableStateFlow<LoginState> = _usersState

    fun login(
            username: String,
            password: String
    ) {
        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            usersState.value = usersState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false,
                warningAction = "",
            )
            return
        }
        if (SettingsModel.currentCompany?.companySS == true) {
            usersState.value = usersState.value.copy(
                warning = SettingsModel.companyAccessWarning,
                isLoading = false,
                warningAction = "",
            )
            return
        }
        usersState.value = usersState.value.copy(
            isLoading = true,
            warning = "",
            warningAction = ""
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllUsers(object : OnResult {
                override fun onSuccess(result: Any) {
                    result as List<User>
                    val size = result.size
                    if (size == 0) {
                        viewModelScope.launch(Dispatchers.Main) {
                            usersState.value = usersState.value.copy(
                                isLoading = false,
                                warning = "no user found!",
                                warningAction = "Register"
                            )
                        }
                    } else {
                        var user: User? = null
                        result.forEach {
                            if (username.equals(
                                    it.userUsername,
                                    ignoreCase = true
                                ) && password.equals(
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
                                    warning = "Username or Password are incorrect!",
                                    warningAction = ""
                                )
                            } else {
                                usersState.value = usersState.value.copy(
                                    selectedUser = user!!,
                                    isLoading = false,
                                    isLoggedIn = true
                                )
                            }
                        }
                    }
                }

                override fun onFailure(
                        message: String,
                        errorCode: Int
                ) {

                }/*repository.getUserByCredentials(username, password, object : OnResult {
                    override fun onSuccess(result: Any) {
                        if (result is User) {
                            viewModelScope.launch(Dispatchers.Main) {
                                usersState.value = usersState.value.copy(
                                    selectedUser = result,
                                    isLoading = false,
                                    isLoggedIn = true
                                )
                            }
                        }
                    }

                    override fun onFailure(message: String, errorCode: Int) {
                        viewModelScope.launch(Dispatchers.Main) {
                            usersState.value = usersState.value.copy(
                                isLoading = false,
                                warning = message,
                                warningAction = "Register"
                            )
                        }
                    }

                })*/
            })
        }
    }
}