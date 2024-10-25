package com.grid.pos.model

import com.grid.pos.data.DataModel

data class ConnectionModel(
        val connectionName: String
) : DataModel() {
    override fun getName(): String {
        return connectionName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

enum class CONNECTION_TYPE(val key: String) {
    LOCAL("LOCAL"), FIRESTORE("FIRESTORE"), SQL_SERVER("SQL_SERVER")
}