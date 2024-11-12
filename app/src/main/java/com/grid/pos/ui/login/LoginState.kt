package com.grid.pos.ui.login

import com.grid.pos.data.User.User
import com.grid.pos.model.Event

data class LoginState(
        var selectedUser: User = User(),
        var needLicense: Boolean = false,
        val isLoggedIn: Boolean = false,
        var isLoading: Boolean = false,
        var needRegistration: Boolean = false,
        var warning: Event<String>? = null,
        var warningAction: String? = null,
)