package com.grid.pos.data

import android.util.Log
import com.grid.pos.model.SettingsModel
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types

/*Grids_web_big companies :
592045FA-5771-4095-AD9A-24DEEC9F318E,E3F3723E-D652-4591-BAF2-338308421A5D,18F082BE-8B13-49D0-87CF-440892778A46,C7F61C93-46C0-47A7-818D-5E1E0A5A9A8A,106F3129-FD52-4EE7-A9B0-679B605D36BB,DF6523E4-BE1B-475F-85CA-8637CD548B80,FDE0B280-1C6A-4521-92AD-8A65A6A01DE0,56244666-F26F-4A53-AFC3-914C6A1A8EDF,F84A238C-B07C-4028-97AD-B494E0E9752A*/
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
            if (mConnection != null && !mConnection!!.isClosed) {
                return
            }
            mConnection = getDatabaseConnection()
        } catch (e: Throwable) {
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
            val connection = getDatabaseConnection(
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
        orderBy: String = "",
        joinSubQuery: String = "",
    ): ResultSet? {
        try {
            val connection = getConnection()
            val cols = columns.joinToString(", ")
            val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
            val query = "SELECT $colPrefix $cols FROM $tableName $joinSubQuery $whereQuery $orderBy"
            val statement = connection.prepareStatement(query)
            return statement.executeQuery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getQueryResult(
        query: String
    ): ResultSet? {
        try {
            val connection = getConnection()
            val statement = connection.prepareStatement(query)
            return statement.executeQuery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun selectFromProcedure(
        procedureName: String,
        params: List<Any>,
    ): ResultSet? {
        try {
            val connection = getConnection()
            val parameters = params.joinToString(", ")
            // Prepare the stored procedure call
            val query =
                "select dbo.$procedureName($parameters) as $procedureName" // Modify with your procedure and parameters
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
            } else if (it is Timestamp) {
                "'$it'"
            } else {
                "$it"
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
            if (value is String) {
                if (value.startsWith("hashBytes")) {
                    "$param=$value"
                } else {
                    "$param='$value'"
                }
            } else if (value is Timestamp) {
                "$param='$value'"
            } else {
                "$param=$value"
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

    fun executeProcedure(
        procedureName: String,
        values: List<Any?>
    ): String? {
        var connection: Connection? = null
        var callableStatement: CallableStatement? = null
        var result: String? = null
        try {
            val params = values.joinToString(", ") { "?" }
            var outputIndex = -1
        /*    var index = -1
            val vals = values.joinToString(", ") {
                index +=1
                if (it is String) {
                    if (it.startsWith("null_int_output")) {
                        outputIndex = index
                        "null"
                    } else if (it.startsWith("null_string_output")) {
                        outputIndex = index
                        "Default"
                    } else {
                        "'$it'"
                    }
                } else if (it is Timestamp) {
                    "'$it'"
                } else {
                    "$it"
                }
            }*/
            connection = getConnection()
            val query = "{call $procedureName($params)}"
            callableStatement = connection.prepareCall(query)

            values.forEachIndexed { index, any ->
                if (any == null) {
                    callableStatement.setNull(index + 1, Types.NULL)
                } else {
                    when (any) {
                        is String -> {
                            if (any.equals("null_int_output", ignoreCase = true)) {
                                outputIndex = index + 1
                                callableStatement.registerOutParameter(index + 1, Types.BIGINT)
                            } else if (any.equals("null_string_output", ignoreCase = true)) {
                                outputIndex = index + 1
                                callableStatement.registerOutParameter(index + 1, Types.VARCHAR)
                            } else if (any.equals("null", ignoreCase = true)) {
                                callableStatement.setNull(index + 1, Types.NULL)
                            } else {
                                callableStatement.setString(index + 1, any)
                            }
                        }

                        is Timestamp -> {
                            callableStatement.setTimestamp(index + 1, any)
                        }

                        is Double -> {
                            callableStatement.setDouble(index + 1, any)
                        }

                        is Int -> {
                            callableStatement.setInt(index + 1, any)
                        }

                        is Boolean -> {
                            callableStatement.setBoolean(index + 1, any)
                        }

                        is Float -> {
                            callableStatement.setFloat(index + 1, any)
                        }

                        else -> {
                            Log.d(SQLServerWrapper::class.java.name, "missing params")
                        }
                    }
                }
            }
            callableStatement.execute()
            if (outputIndex >= 0) {
                result = callableStatement.getString(outputIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            callableStatement?.close()
            if (mConnection == null) {
                connection?.close()
            }
        }
        return result
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

    fun getConnection(): Connection {
        if (mConnection != null && !mConnection!!.isClosed) {
            return mConnection!!
        }
        return getDatabaseConnection()
    }
}