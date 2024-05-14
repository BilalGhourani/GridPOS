package com.grid.pos.ui.user

import com.grid.pos.data.User.User

data class ManageUsersState(
        val users: MutableList<User> = mutableListOf(),
        var selectedUser: User = User(),
        val isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: String? = null,
)