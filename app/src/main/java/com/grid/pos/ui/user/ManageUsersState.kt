package com.grid.pos.ui.user

import com.grid.pos.data.Company.Company
import com.grid.pos.data.User.User

data class ManageUsersState(
    val users: MutableList<User> = mutableListOf(),
    val companies: MutableList<Company> = mutableListOf(),
    var selectedUser: User = User(),
    val isLoading: Boolean = false,
    val warning: String? = null,
    )