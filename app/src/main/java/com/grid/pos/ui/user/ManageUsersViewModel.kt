package com.grid.pos.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.utils.Extension.encryptCBC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
        private val userRepository: UserRepository
) : ViewModel() {

    private val _manageUsersState = MutableStateFlow(ManageUsersState())
    val manageUsersState: MutableStateFlow<ManageUsersState> = _manageUsersState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchUsers()
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

    private suspend fun fetchUsers() {
        val listOfUsers =  userRepository.getAllUsers()
        viewModelScope.launch(Dispatchers.Main) {
            manageUsersState.value = manageUsersState.value.copy(
                users = listOfUsers
            )
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
                users.add(addedModel)
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = users,
                        selectedUser = addedModel,
                        isLoading = false,
                        clear = true
                    )
                }
            } else {
                userRepository.update(user)
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        selectedUser = user,
                        isLoading = false,
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
            userRepository.delete(user)
            val users = manageUsersState.value.users
            users.remove(user)
            viewModelScope.launch(Dispatchers.Main) {
                manageUsersState.value = manageUsersState.value.copy(
                    users = users,
                    selectedUser = User(),
                    isLoading = false,
                    clear = true
                )
            }
        }
    }

}