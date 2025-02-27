package com.grid.pos.ui.user

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.User
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.PopupModel
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
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _state = MutableStateFlow(ManageUsersState())
    val state: MutableStateFlow<ManageUsersState> = _state

    var currentUser: User = User()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun updateUser(user: User) {
        state.value = state.value.copy(
            user = user
        )
    }

    fun resetState() {
        currentUser = User()
        state.value = state.value.copy(
            user = currentUser.copy()
        )
    }

    fun checkChanges(callback: () -> Unit) {
        if (state.value.user.didChanged(currentUser)) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        resetState()
                        callback.invoke()
                    }
                    onConfirmation = {
                        save {
                            callback.invoke()
                        }
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    cancelable = false
                })
        } else {
            callback.invoke()
        }
    }

    fun fetchUsers() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfUsers = userRepository.getAllUsers()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    users = listOfUsers
                )
                showLoading(false)
            }
        }
    }

    fun save(callback: (Boolean) -> Unit = {}) {
        val user = state.value.user
        if (user.userName.isNullOrEmpty() || user.userUsername.isNullOrEmpty() || user.userPassword.isNullOrEmpty()) {
            showLoading(false)
            showWarning("Please fill all inputs")
            return
        }
        showLoading(true)
        val isInserting = user.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            user.userPassword = user.userPassword!!.encryptCBC()
            if (isInserting) {
                user.prepareForInsert()
                if (sharedViewModel.isRegistering) {
                    user.userPosMode = true
                    user.userTableMode = true
                }
                val dataModel = userRepository.insert(user)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as User
                    val users = state.value.users
                    if (users.isNotEmpty()) {
                        users.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            users = users
                        )
                        resetState()
                        showLoading(false)
                        if (sharedViewModel.isRegistering) {
                            showPopup(
                                PopupModel(
                                    onDismissRequest = {
                                        callback.invoke(true)
                                    },
                                    onConfirmation = {
                                        callback.invoke(true)
                                    },
                                    dialogText = "User saved successfully and Registration is done.",
                                    positiveBtnText = "Login",
                                    negativeBtnText = null,
                                    cancelable = false
                                )
                            )
                        } else {
                            showWarning("User saved successfully.")
                            callback.invoke(false)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val dataModel = userRepository.update(user)
                if (dataModel.succeed) {
                    val users = state.value.users.toMutableList()
                    val index = users.indexOfFirst { it.userId == user.userId }
                    if (index >= 0) {
                        users.removeAt(index)
                        users.add(
                            index,
                            user
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            users = users,
                        )
                        resetState()
                        showLoading(false)
                        showWarning("User saved successfully.")
                        callback.invoke(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun delete() {
        val user = state.value.user
        if (user.userId.isEmpty()) {
            showWarning("Please select an user to delete")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(user.userId)) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("You can't delete this User,because it has related data!")
                }
                return@launch
            }
            val dataModel = userRepository.delete(user)
            if (dataModel.succeed) {
                val users = state.value.users
                users.remove(user)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        users = users
                    )
                    resetState()
                    showLoading(false)
                    showWarning("successfully deleted.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun hasRelations(userID: String): Boolean {
        if (userID == SettingsModel.currentUser?.userId) return true

        if (thirdPartyRepository.getOneThirdPartyByUserID(userID) != null) {
            return true
        }

        if (invoiceHeaderRepository.getOneInvoiceByUserID(userID) != null) return true

        return false
    }
}