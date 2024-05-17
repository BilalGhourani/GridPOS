package com.grid.pos

data class ActivityState(
    var isLoggedIn: Boolean = false,
    var warning: String? = null,
    var forceLogout: Boolean = false
)
