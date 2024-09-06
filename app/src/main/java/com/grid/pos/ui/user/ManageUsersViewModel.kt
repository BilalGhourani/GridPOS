package com.grid.pos.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
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
    var currentUser: User? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun fillCachedUsers(users: MutableList<User> = mutableListOf()) {
        if (manageUsersState.value.users.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageUsersState.value = manageUsersState.value.copy(
                    users = users
                )
            }
        }
    }

    fun fetchUsers() {
        manageUsersState.value = manageUsersState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfUsers = userRepository.getAllUsers()
            withContext(Dispatchers.Main) {
                manageUsersState.value = manageUsersState.value.copy(
                    users = listOfUsers,
                    isLoading = false
                )
            }
        }
    }

    fun saveUser(user: User) {
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
                val addedModel = userRepository.insert(user)
                val users = manageUsersState.value.users
                if(users.isNotEmpty()) {
                    users.add(addedModel)
                }
                currentUser = null
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = users,
                        selectedUser = addedModel,
                        isLoading = false,
                        warning = Event("User saved successfully."),
                        clear = true
                    )
                }
            } else {
                userRepository.update(user)
                currentUser = null
                withContext(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        selectedUser = user,
                        isLoading = false,
                        warning = Event("User saved successfully."),
                        clear = true
                    )
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
            userRepository.delete(user)
            val users = manageUsersState.value.users
            users.remove(user)
            currentUser = null
            withContext(Dispatchers.Main) {
                manageUsersState.value = manageUsersState.value.copy(
                    users = users,
                    selectedUser = User(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

    private suspend fun hasRelations(userID: String): Boolean {
        if (userID.equals(SettingsModel.currentUserId)) return true

        if (thirdPartyRepository.getOneThirdPartyByUserID(userID) != null) return true

        if (invoiceHeaderRepository.getOneInvoiceByUserID(userID) != null) return true

        return false
    }
}