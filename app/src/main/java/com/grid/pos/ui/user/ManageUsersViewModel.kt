package com.grid.pos.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _manageUsersState = MutableStateFlow(ManageUsersState())
    val manageUsersState: MutableStateFlow<ManageUsersState> = _manageUsersState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchUsers()
            fetchCompanies()
        }
    }

    private suspend fun fetchUsers() {
        userRepository.getAllUsers(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfUsers = mutableListOf<User>()
                (result as List<User>).forEach {
                    listOfUsers.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = listOfUsers
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    private suspend fun fetchCompanies() {
        companyRepository.getAllCompanies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfCompanies = mutableListOf<Company>()
                (result as List<Company>).forEach {
                    listOfCompanies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        companies = listOfCompanies
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    fun saveUser(user: User) {
        if (user.userName.isNullOrEmpty() || user.userUsername.isNullOrEmpty() || user.userPassword.isNullOrEmpty() || user.userCompanyId.isNullOrEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
            )
            return
        }
        manageUsersState.value = manageUsersState.value.copy(
            isLoading = true
        )
        val isInserting = user.userDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as User
                    val users = manageUsersState.value.users
                    if (isInserting) users.add(addedModel)
                    manageUsersState.value = manageUsersState.value.copy(
                        users = users,
                        selectedUser = addedModel,
                        isLoading = false,
                        clear = true
                    )
                }
            }

            override fun onFailure(message: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                user.userId = Utils.generateRandomUuidString()
                userRepository.insert(user, callback)
            } else {
                userRepository.update(user, callback)
            }
        }
    }

    fun deleteSelectedUser(user: User) {
        if (user.userDocumentId.isNullOrEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = "Please select an user to delete",
                isLoading = false
            )
            return
        }
        manageUsersState.value = manageUsersState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            userRepository.delete(user, object : OnResult {
                override fun onSuccess(result: Any) {
                    val users = manageUsersState.value.users
                    val position =
                        users.indexOfFirst { user.userId.equals(it.userId, ignoreCase = true) }
                    if (position >= 0) {
                        users.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            selectedUser = result as User,
                            isLoading = false
                        )
                    }
                }

                override fun onFailure(message: String) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}