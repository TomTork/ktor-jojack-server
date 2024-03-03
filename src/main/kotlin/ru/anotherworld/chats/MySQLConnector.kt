package ru.anotherworld.chats

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class MySQLConnector(nameDB: String) {
    private val path = "jdbc:mysql:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/Messenger/$nameDB.db"
    private val tableName = "messages"
    private val initFirst = """
        CREATE TABLE IF NOT EXISTS $tableName(
        id INTEGER PRIMARY KEY,
        author TEXT,
        message TEXT,
        time INTEGER
        );
    """.trimIndent()
    private var conn: Connection? = null
    init {
        try {
            conn = DriverManager.getConnection(path)
            if (conn != null){
                val statement = conn!!.createStatement()
                statement.execute(initFirst)
                conn!!.commit()
                conn!!.close()
            }
        } catch (e: SQLException){
            println("MySQLConnector: ${e.message}")
        }
    }
}