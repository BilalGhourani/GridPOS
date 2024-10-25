package com.grid.pos.model

import com.grid.pos.data.DataModel

data class OrientationModel(
        val orientatioName: String
) : DataModel() {
    override fun getName(): String {
        return orientatioName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

enum class ORIENTATION_TYPE(val key: String) {
    PORTRAIT("Portrait"), LANDSCAPE("Landscape"), DEVICE_SENSOR("Device Sensor")
}

