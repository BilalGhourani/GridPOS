package com.grid.pos

import com.grid.pos.model.Event

data class ActivityState(
    var isLoggedIn: Boolean = false,
    var warning: Event<String>? = null,
    var forceLogout: Boolean = false
)
