package com.grid.pos.model

data class LoginResponseModel(
    var encrypted: String? = null,
    var success: Int = -1,
    var message: String? = null
)
