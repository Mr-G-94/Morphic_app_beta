package com.morphiclabs.agents

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.morphiclabs.core.base.AgentContract

class KnowledgeAgent(private val context: Context) : AgentContract {

    override suspend fun canHandle(command: String): Boolean {
        return command.startsWith("db:", ignoreCase = true)
    }

    override suspend fun execute(input: String): String {
        val sqlQuery = input.substringAfter("db:").trim()
        if (sqlQuery.isEmpty()) {
            return "Error: SQL query is empty."
        }

        return try {
            // Abre o crea la base de datos morphic_gateway.db en el contexto privado de la app
            val db: SQLiteDatabase = context.openOrCreateDatabase(
                "morphic_gateway.db",
                Context.MODE_PRIVATE,
                null
            )

            db.use { database ->
                database.rawQuery(sqlQuery, null).use { cursor ->
                    val columnCount = cursor.columnCount
                    if (columnCount == 0) {
                        return@use "Query executed successfully. No columns returned."
                    }

                    val resultBuilder = StringBuilder()
                    
                    // Obtener y formatear los nombres de las columnas
                    val headers = (0 until columnCount).map { cursor.getColumnName(it) }
                    val headerLine = headers.joinToString(" | ")
                    resultBuilder.append(headerLine).append("\n")
                    resultBuilder.append("-".repeat(headerLine.length)).append("\n")

                    var rowCount = 0
                    while (cursor.moveToNext()) {
                        val row = (0 until columnCount).map { index ->
                            try {
                                cursor.getString(index) ?: "NULL"
                            } catch (e: Exception) {
                                "BLOB/Error"
                            }
                        }
                        resultBuilder.append(row.joinToString(" | ")).append("\n")
                        rowCount++
                    }

                    if (rowCount == 0) {
                        "Query executed successfully. No rows returned."
                    } else {
                        resultBuilder.toString().trimEnd()
                    }
                }
            }
        } catch (e: Exception) {
            "Database error: ${e.localizedMessage ?: e.message}"
        }
    }
}
