package com.grid.pos.ui.login

import com.grid.pos.data.User.User
import com.grid.pos.model.Event

data class LoginState(
        var selectedUser: User = User(),
        val isLoggedIn: Boolean = false,
        var isLoading: Boolean = false,
        var warning: Event<String>? = null,
        val warningAction: String? = null,
)