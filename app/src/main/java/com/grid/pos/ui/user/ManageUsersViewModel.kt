package com.grid.pos.ui.user

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.User
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.Extension.encryptCBC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : BaseViewModel() {

    private val _manageUsersState = MutableStateFlow(ManageUsersState())
    val manageUsersState: MutableStateFlow<ManageUsersState> = _manageUsersState
    var currentUser: User = User()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        manageUsersState.value = manageUsersState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun fetchUsers() {
        manageUsersState.value = manageUsersState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = userRepository.getAllUsers()
            if (dataModel.succeed) {
                val listOfUsers = convertToMutableList(
                    dataModel.data,
                    User::class.java
                )
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = listOfUsers,
                        isLoading = false
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        isLoading = false,
                        warning = Event(dataModel.message),
                    )
                }
            }
        }
    }

    fun saveUser(
            user: User,
            isRegistering: Boolean
    ) {
        if (user.userName.isNullOrEmpty() || user.userUsername.isNullOrEmpty() || user.userPassword.isNullOrEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = Event("Please fill all inputs"),
                isLoading = false
            )
            return
        }
        manageUsersState.value = manageUsersState.value.copy(
            isLoading = true
        )
        val isInserting = user.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            user.userPassword = user.userPassword!!.encryptCBC()
            if (isInserting) {
                user.prepareForInsert()
                if (isRegistering) {
                    user.userPosMode = true
                    user.userTableMode = true
                }
                val dataModel = userRepository.insert(user)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as User
                    val users = manageUsersState.value.users
                    if (users.isNotEmpty()) {
                        users.add(addedModel)
                    }
                    val msg = if (isRegistering) {
                        "User saved successfully and Registration is done."
                    } else {
                        "User saved successfully."
                    }
                    withContext(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            users = users,
                            selectedUser = addedModel,
                            isLoading = false,
                            warning = Event(msg),
                            action = "done",
                            clear = true
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            isLoading = false,
                            warning = Event(dataModel.message),
                        )
                    }
                }
            } else {
                val dataModel = userRepository.update(user)
                if (dataModel.succeed) {
                    val index = manageUsersState.value.users.indexOfFirst { it.userId == user.userId }
                    if (index >= 0) {
                        manageUsersState.value.users.removeAt(index)
                        manageUsersState.value.users.add(
                            index,
                            user
                        )
                    }
                    withContext(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            selectedUser = user,
                            isLoading = false,
                            warning = Event("User saved successfully."),
                            clear = true
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            isLoading = false,
                            warning = Event(dataModel.message),
                        )
                    }
                }
            }
        }
    }

    fun deleteSelectedUser(user: User) {
        if (user.userId.isEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = Event("Please select an user to delete"),
                isLoading = false
            )
            return
        }
        manageUsersState.value = manageUsersState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(user.userId)) {
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        warning = Event("You can't delete this User,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = userRepository.delete(user)
            if (dataModel.succeed) {
                val users = manageUsersState.value.users
                users.remove(user)
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = users,
                        selectedUser = User(),
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        isLoading = false,
                        warning = Event(dataModel.message),
                    )
                }
            }
        }
    }

    private suspend fun hasRelations(userID: String): Boolean {
        if (userID == SettingsModel.currentUser?.userId) return true

        val thirdPartyDataModel = thirdPartyRepository.getOneThirdPartyByUserID(userID)
        if (thirdPartyDataModel.succeed && thirdPartyDataModel.data != null) {
            return true
        }

        if (invoiceHeaderRepository.getOneInvoiceByUserID(userID) != null) return true

        return false
    }
}