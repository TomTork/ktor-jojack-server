package ru.anotherworld.chats.two

import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

//DEPRECATED
class MessengerController2(nameDB: String) {
    private val path = "jdbc:postgresql://localhost/$nameDB"
    private val tableName = "messenger"
    private val initFirst = """
        CREATE TABLE IF NOT EXISTS $tableName(
            id int AUTO_INCREMENT PRIMARY KEY,
            author varchar(64),
            message varchar(8192),
            time int
        );
    """.trimIndent()
    private val user = "user"
    private val password = "password"
    private var conn: Connection? = null
    init {
        try {
            conn = DriverManager.getConnection(path, user, password)

            val statement = conn!!.createStatement()
            statement.execute(initFirst)
            statement.close()
        } catch (e: SQLException){
            println(e)
        }
    }
    private fun getMessage(id: Int): TMessage{
        val sql = "SELECT id,author,message,time from $tableName where id=$id"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            return TMessage(
                res.getInt("id"),
                res.getString("author"),
                res.getString("message"),
                res.getLong("time")
            )
        } catch (e: SQLException){
            println(e.message)
            return TMessage(-1, "", "", -1)
        }
    }
    fun getRangeMessage(startIndex: Int, endIndex: Int): List<TMessage>{
        val arr = ArrayList<TMessage>()
        for (i in startIndex..endIndex) arr.add(getMessage(i))
        return arr.toList()
    }
    fun getAllMessage(): List<TMessage>{
        val sql = "SELECT * FROM $tableName"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            val arr = ArrayList<TMessage>()
            while (res.next()){
                arr.add(TMessage(res.getInt("id"),
                    res.getString("author"),
                    res.getString("message"),
                    res.getLong("time"))
                )
            }
            return arr.toList()
        } catch (e: SQLException){
            println(e.message)
            return arrayListOf()
        }
    }
    fun getLengthMessages(): Int{
        val sql = "SELECT * FROM $tableName"
        return try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            val result = ArrayList<Int>()
            while (res.next()){
                result.add(res.getInt("id"))
            }
            println(result.size)
            result.size ?: 1
        } catch (e: SQLException){
            println(e.message)
            1
        }
    }
    fun insertMessage(author: String, message: String, time: Long){
        val sql = "INSERT INTO $tableName(author,message,time) values(?,?,?)"
        try {
            val statement = conn!!.prepareStatement(sql)
            statement.setString(1, author)
            statement.setString(2, message)
            statement.setLong(3, time)
            statement.executeUpdate()
            statement.close()
        } catch (e: SQLException){
            println(e.message)
        }
    }
}