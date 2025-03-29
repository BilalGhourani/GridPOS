package com.grid.pos.model

import org.json.JSONObject

data class LoginResponseModel(
    var encrypted: String? = null,
    var success: Int = -1,
    var message: String? = null,
    var user: JSONObject? = null
)
