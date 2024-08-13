package com.grid.pos.model

import com.grid.pos.data.DataModel

enum class CONNECTION_TYPE(val key: String) {
    LOCAL("LOCAL"), FIRESTORE("FIRESTORE"), SQL_SERVER("SQL_SERVER")
}

data class ConnectionModel(
        val connectionName: String
) : DataModel() {
    override fun getName(): String {
        return connectionName
    }
}

enum class ORIENTATION_TYPE(val key: String) {
    PORTRAIT("Portrait"), LANDSCAPE("Landscape"), DEVICE_SENSOR("Device Sensor")
}

data class OrientationModel(
        val orientatioName: String
) : DataModel() {
    override fun getName(): String {
        return orientatioName
    }
}
