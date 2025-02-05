package com.grid.pos.ui.user

import com.grid.pos.data.user.User
import com.grid.pos.model.Event

data class ManageUsersState(
        val users: MutableList<User> = mutableListOf(),
        val user:User = User(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val action: String? = null,
)