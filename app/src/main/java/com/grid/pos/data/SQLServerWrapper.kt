package com.grid.pos.data

import com.grid.pos.model.SettingsModel
import org.json.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

object SQLServerWrapper {

    private fun getDatabaseConnection(): Connection {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return DriverManager.getConnection(
            SettingsModel.getSqlServerDbPath(),
            SettingsModel.sqlServerDbUser,
            SettingsModel.sqlServerDbPassword
        )
    }

    fun getListOf(
            tableName: String,
            columns: MutableList<String>,
            where: String,
            joinSubQuery: String = "",
    ): List<JSONObject> {
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        val result = mutableListOf<JSONObject>()
        try {
            connection = getDatabaseConnection()
            val cols = columns.joinToString(", ")
            val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
            val query = "SELECT $cols FROM $tableName $joinSubQuery $whereQuery"
            statement = connection.prepareStatement(query)
            resultSet = statement.executeQuery()
            if (cols.contains("*")) {
                // Get the ResultSetMetaData
                val metaData = resultSet.metaData

                // Get the number of columns
                val columnCount = metaData.columnCount
                columns.clear()
                // Iterate through the columns and print their names
                for (i in 1..columnCount) {
                    columns.add(metaData.getColumnName(i))
                }
            }
            while (resultSet.next()) {
                val obj = JSONObject()
                for (columnName in columns) {
                    val columnValue = resultSet.getObject(columnName)
                    obj.put(
                        columnName,
                        columnValue
                    )
                }
                result.add(obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
        return result
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
        val vals = columns.joinToString(", ") { "?" }
        val sqlQuery = "INSERT INTO $tableName ($cols) VALUES ($vals)"
        runDbQuery(
            sqlQuery,
            values
        )
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
        val setStatement = columns.joinToString(", ") { "$it = ?" }
        val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
        val sqlQuery = "UPDATE $tableName SET $setStatement $whereQuery"
        runDbQuery(
            sqlQuery,
            values
        )
    }

    fun delete(
            tableName: String,
            where: String,
            innerJoin: String = ""
    ) {
        val whereQuery = if (where.isNotEmpty()) "WHERE $where " else ""
        val sqlQuery = "DELETE FROM $tableName $innerJoin $whereQuery"
        runDbQuery(
            sqlQuery,
            listOf()
        )
    }

    private fun runDbQuery(
            query: String,
            params: List<Any?>
    ): Boolean {
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        return try {
            connection = getDatabaseConnection()
            statement = connection.prepareStatement(query)

            params.forEachIndexed { index, param ->
                statement.setObject(
                    index + 1,
                    param
                )
            }

            statement.executeUpdate() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            statement?.close()
            connection?.close()
        }
    }
}