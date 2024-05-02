package com.grid.pos.ui.login

import com.grid.pos.data.User.User

data class LoginState(
    var selectedUser: User = User(),
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val warning: String? = null,
    val warningAction: String? = null,
)