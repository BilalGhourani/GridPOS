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
