package com.grid.pos.interfaces

interface OnResult {
    fun onSuccess(result: Any)

    fun onFailure(message: String, errorCode: Int = 0)
}