package ru.anotherworld.chats.two

import kotlinx.serialization.Serializable
import ru.anotherworld.globalPath
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

//DEPRECATED
class MessengerController(nameDB: String) {
    private val path = "jdbc:sqlite:$globalPath/sqldatabase/Messenger/$nameDB.db"
    private val tableName = "messenger"
    private val tableName2 = "op"
    private val initializeFirst = "CREATE TABLE IF NOT EXISTS $tableName(" +
            "id INTEGER PRIMARY KEY," +
            "author TEXT," +
            "message TEXT," +
            "time INTEGER);"
    private val initializeSecond = "CREATE TABLE IF NOT EXISTS $tableName2(" +
            "id INTEGER PRIMARY KEY," +
            "op1 TEXT," +
            "op2 TEXT);"
    private var conn: Connection? = null
    init {
        try {
            conn = DriverManager.getConnection(path)
            if (conn != null){
                val statement = conn!!.createStatement()
                statement.execute(initializeFirst)
                statement.execute(initializeSecond)

                //Insert open keys


                statement.close()
            }
        } catch (e: SQLException){
            println(e.message)
        }
    }
    fun dropTable(){
        val sql = "DROP TABLE $tableName"
        val sql2 = "DROP TABLE $tableName2"
        val statement = conn!!.createStatement()
        statement.execute(sql)
        statement.execute(sql2)
        statement.close()
    }
    fun getPairOpenKey(): TPairOpenKey{
        val sql = "SELECT op1,op2 FROM $tableName2 WHERE id=1"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            return TPairOpenKey(
                res.getString("op1"),
                res.getString("op2")
            )
        } catch (e: SQLException){
            println(e.message)
            return TPairOpenKey("", "")
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
    private fun getLengthOp(): Int{
        val sql = "SELECT id FROM $tableName2"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            var result = ""
            while (res.next()){
                result += res.getInt("id").toString()
            }
            statement.close()
            return result.toInt()
        } catch (e: SQLException){
            return -1
        }
    }
    fun setOp1(key: String){
        val lengthOp = getLengthOp()
        if (lengthOp == -1){ //Create
            val sql = "INSERT INTO $tableName2(op1,op2) values(?,?)"
            try {
                val statement = conn!!.prepareStatement(sql)
                statement.setString(1, key)
                statement.setString(2, "")
                statement.executeUpdate()
                statement.close()
            } catch (e: SQLException){
                println(e.message)
            }
        }
        else { //UPDATE
            val sql = "UPDATE $tableName2 set op1=? where id=1"
            try {
                val statement = conn!!.prepareStatement(sql)
                statement.setString(1, key)
                statement.executeUpdate()
                statement.close()
            } catch (e: SQLException){
                println(e.message)
            }
        }
    }
    fun setOp2(key: String){
        val lengthOp = getLengthOp()
        if (lengthOp == -1){ //Create
            val sql = "INSERT INTO $tableName2(op1,op2) values(?,?)"
            try {
                val statement = conn!!.prepareStatement(sql)
                statement.setString(1, "")
                statement.setString(2, key)
                statement.executeUpdate()
                statement.close()
            } catch (e: SQLException){
                println(e.message)
            }
        }
        else { //UPDATE
            val sql = "UPDATE $tableName2 set op2=? where id=1"
            try {
                val statement = conn!!.prepareStatement(sql)
                statement.setString(1, key)
                statement.executeUpdate()
                statement.close()
            } catch (e: SQLException){
                println(e.message)
            }
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
    fun close(){
        conn!!.close()
    }
}

@Serializable
data class TMessage(
    val id: Int,
    val author: String,
    val message: String,
    val timestamp: Long
)

@Serializable
data class TPairOpenKey(
    val op1: String,
    val op2: String
)