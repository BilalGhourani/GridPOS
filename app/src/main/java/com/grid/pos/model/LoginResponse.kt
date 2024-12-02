package com.grid.pos.model

import com.grid.pos.data.user.User

data class LoginResponse(
        val allUsersSize: Int = 0,
        var user: User? = null,
        val error: String? = null
)
