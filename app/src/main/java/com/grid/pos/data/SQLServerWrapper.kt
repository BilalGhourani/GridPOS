package com.grid.pos.data

import com.grid.pos.model.SettingsModel
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

object SQLServerWrapper {

    private var mConnection: Connection? = null

    private fun getDatabaseConnection(
            serverPath: String? = SettingsModel.sqlServerPath,
            dbName: String? = SettingsModel.sqlServerDbName,
            serverName: String? = SettingsModel.sqlServerName,
            username: String? = SettingsModel.sqlServerDbUser,
            password: String? = SettingsModel.sqlServerDbPassword
    ): Connection {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return DriverManager.getConnection(
            "jdbc:jtds:sqlserver://${serverPath}/$dbName;instance=$serverName;encrypt=true",
            username,
            password
        )
    }

    fun openConnection() {
        try {
            mConnection = getDatabaseConnection()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeConnection() {
        try {
            mConnection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeResultSet(resultSet: ResultSet) {
        try {
            val statement = resultSet.statement
            if (mConnection == null && !statement.connection.isClosed) {
                statement.connection.close()
            }
            if (!statement.isClosed) {
                statement.close()
            }
            if (!resultSet.isClosed) {
                resultSet.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isConnectionSucceeded(
            serverPath: String? = SettingsModel.sqlServerPath,
            dbName: String? = SettingsModel.sqlServerDbName,
            serverName: String? = SettingsModel.sqlServerName,
            username: String? = SettingsModel.sqlServerDbUser,
            passwrod: String? = SettingsModel.sqlServerDbPassword
    ): String {
        return try {
           val connection =  getDatabaseConnection(
                serverPath,
                dbName,
                serverName,
                username,
                passwrod
            )
            val query = "SELECT TOP 1 * FROM company"
            val statement = connection.prepareStatement(query)
             statement.executeQuery()
            connection.close()
            "Connection Successful!"
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "Failed to connect!"
        }
    }

    fun getListOf(
            tableName: String,
            colPrefix: String = "",
            columns: MutableList<String>,
            where: String,
            joinSubQuery: String = "",
    ): ResultSet? {
        try {
            val connection = getConnection()
            val cols = columns.joinToString(", ")
            val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
            val query = "SELECT $colPrefix $cols FROM $tableName $joinSubQuery $whereQuery"
            val statement = connection.prepareStatement(query)
            return statement.executeQuery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun executeProcedure(
            procedureName: String,
            params: List<Any>,
    ): ResultSet? {
        try {
            val connection = getConnection()
            val parameters = params.joinToString(", ")
            // Prepare the stored procedure call
            val query = "select dbo.$procedureName($parameters) as $procedureName" // Modify with your procedure and parameters
            val statement = connection.prepareStatement(query)
            return statement.executeQuery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun insert(
            tableName: String,
            columns: List<String>,
            values: List<Any?>
    ) {
        if (columns.size != values.size) {
            return
        }
        val cols = columns.joinToString(", ")
        val vals = values.joinToString(", ") {
            if (it is String) {
                if (it.startsWith("hashBytes")) {
                    it
                } else {
                    "'$it'"
                }
            } else {
                "'$it'"
            }
        }
        runDbQuery("INSERT INTO $tableName ($cols) VALUES ($vals)")
    }

    fun update(
            tableName: String,
            columns: List<String>,
            values: List<Any?>,
            where: String
    ) {
        if (columns.size != values.size) {
            return
        }
        //val setStatement = columns.joinToString(", ") { "$it = ?" }
        // Combine the lists into the desired format
        val setStatement = columns.zip(values) { param, value ->
            if (value is String && value.startsWith("hashBytes")) {
                "$param=$value"
            } else {
                "$param='$value'"
            }
        }.joinToString(", ")
        val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
        runDbQuery("UPDATE $tableName SET $setStatement $whereQuery")
    }

    fun delete(
            tableName: String,
            where: String,
            innerJoin: String = ""
    ) {
        val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
        runDbQuery("DELETE FROM $tableName $innerJoin $whereQuery")
    }

    private fun runDbQuery(
            query: String,
            params: List<Any?>? = null
    ): Boolean {
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        var isSuccess: Boolean
        try {
            connection = getConnection()
            statement = connection.prepareStatement(query)

            params?.forEachIndexed { index, param ->
                statement.setObject(
                    index + 1,
                    param
                )
            }
            val executeVal = statement.executeUpdate()
            isSuccess = executeVal > 0
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccess = false
        } finally {
            statement?.close()
            if (mConnection == null) {
                connection?.close()
            }
        }
        return isSuccess
    }

    private fun getConnection(): Connection {
        if (mConnection != null && !mConnection!!.isClosed) {
            return mConnection!!
        }
        return getDatabaseConnection()
    }
}