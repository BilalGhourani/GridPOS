package com.grid.pos.ui.user

import com.grid.pos.data.user.User

data class ManageUsersState(
        val users: MutableList<User> = mutableListOf(),
        val user:User = User()
)