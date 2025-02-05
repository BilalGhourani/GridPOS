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
        updateUser(User())
        state.value = state.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun isAnyChangeDone():Boolean{
        return state.value.user.didChanged(currentUser)
    }

    fun fetchUsers() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfUsers = userRepository.getAllUsers()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    users = listOfUsers,
                    isLoading = false
                )
            }
        }
    }

    fun save(
        isRegistering: Boolean
    ) {
        val user = state.value.user
        if (user.userName.isNullOrEmpty() || user.userUsername.isNullOrEmpty() || user.userPassword.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please fill all inputs"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
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
                    val users = state.value.users
                    if (users.isNotEmpty()) {
                        users.add(addedModel)
                    }
                    val msg = if (isRegistering) {
                        "User saved successfully and Registration is done."
                    } else {
                        "User saved successfully."
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            users = users,
                            isLoading = false,
                            warning = Event(msg),
                            action = "done",
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null
                        )
                    }
                }
            } else {
                val dataModel = userRepository.update(user)
                if (dataModel.succeed) {
                    val index =
                        state.value.users.indexOfFirst { it.userId == user.userId }
                    if (index >= 0) {
                        state.value.users.removeAt(index)
                        state.value.users.add(
                            index,
                            user
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = Event("User saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val user = state.value.user
        if (user.userId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select an user to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(user.userId)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("You can't delete this User,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = userRepository.delete(user)
            if (dataModel.succeed) {
                val users = state.value.users
                users.remove(user)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        users = users,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = null
                    )
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