package com.grid.pos.utils

object Extension {
    fun Long?.isNullOrZero(): Boolean {
        return this != null && this != 0L
    }
}