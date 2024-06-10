package com.grid.pos.model

import com.grid.pos.data.User.User

data class LoginResponse(
    val allUsersSize: Int = 0,
    var user: User? = null
)
