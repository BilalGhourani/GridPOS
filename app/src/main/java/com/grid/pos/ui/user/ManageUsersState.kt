package com.grid.pos.ui.user

import com.grid.pos.data.User.User
import com.grid.pos.model.Event

data class ManageUsersState(
        val users: MutableList<User> = mutableListOf(),
        var selectedUser: User = User(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val action: String? = null,
)