package com.grid.pos.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
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
        userRepository.getAllUsers(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfUsers = mutableListOf<User>()
                (result as List<*>).forEach {
                    listOfUsers.add(it as User)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        users = listOfUsers
                    )
                }
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {

            }

        })
    }

    fun saveUser(user: User) {
        if (user.userName.isNullOrEmpty() || user.userUsername.isNullOrEmpty() || user.userPassword.isNullOrEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = Event("Please fill all inputs"), isLoading = false
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
                        users = users, selectedUser = addedModel, isLoading = false, clear = true
                    )
                }
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageUsersState.value = manageUsersState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        CoroutineScope(Dispatchers.IO).launch {
            user.userPassword = user.userPassword!!.encryptCBC()
            if (isInserting) {
                user.prepareForInsert()
                userRepository.insert(user, callback)
            } else {
                userRepository.update(user, callback)
            }
        }
    }

    fun deleteSelectedUser(user: User) {
        if (user.userId.isEmpty()) {
            manageUsersState.value = manageUsersState.value.copy(
                warning = Event("Please select an user to delete"), isLoading = false
            )
            return
        }
        manageUsersState.value = manageUsersState.value.copy(
            warning = null, isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            userRepository.delete(user, object : OnResult {
                override fun onSuccess(result: Any) {
                    val users = manageUsersState.value.users
                    val position = users.indexOfFirst {
                        user.userId.equals(
                            it.userId, ignoreCase = true
                        )
                    }
                    if (position >= 0) {
                        users.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageUsersState.value = manageUsersState.value.copy(
                            users = users, selectedUser = User(), isLoading = false, clear = true
                        )
                    }
                }

                override fun onFailure(
                        message: String,
                        errorCode: Int
                ) {
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