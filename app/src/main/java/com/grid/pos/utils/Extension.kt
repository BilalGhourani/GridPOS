package com.grid.pos.utils

import androidx.compose.ui.graphics.Color

object Extension {
    fun Long?.isNullOrZero(): Boolean {
        return this != null && this != 0L
    }

    fun Float.toColorInt(): Int = (this * 255 + 0.5f).toInt()

    fun Color.toHexCode(): String {
        val red = this.red * 255
        val green = this.green * 255
        val blue = this.blue * 255
        return String.format("#%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
    }
}