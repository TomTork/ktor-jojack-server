package ru.anotherworld.chats.two

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import ru.anotherworld.globalPath
import ru.anotherworld.utils.DatabaseSingletonVkPostDatabase
import java.io.File
import kotlin.concurrent.thread

var name: String? = null

class MessengerController3(nameDB: String){
    init {
        name = nameDB
    }
    private object MessengerTable : Table("messenger"){
        val id = integer("id").autoIncrement()

        val author = varchar("author", 64)
        val message = varchar("message", 8192)
        val timestamp = long("time")

        override val primaryKey = PrimaryKey(id)
    }
    object DatabaseSingletonMessenger{
//        private fun createHikariDataSource(
//            url: String,
//            driver: String
//        ) = HikariDataSource(HikariConfig().apply {
//            driverClassName = driver
//            jdbcUrl = url
//            maximumPoolSize = 2
//            isAutoCommit = false
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
//            validate()
//        })

//        val database = Database.connect("jdbc:h2://localhost:5432/$name",
//            driver = "org.postgresql.Driver")
        var database: Database? = null
        fun createHikariDataSource(
            url: String,
            driver: String
        ) = HikariDataSource(HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            maximumPoolSize = 2
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })
        fun init(){
            database = Database.connect(
                createHikariDataSource(
                    url = "jdbc:h2:$globalPath/sqldatabase/messenger/$name;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                    driver = "org.h2.Driver"
                    )
                )
            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(tables = arrayOf(MessengerTable))
            }
        }

    }
    init {
        DatabaseSingletonMessenger.init()
    }
    @Serializable
    data class Messenger(
        val author: String,
        val message: String,
        val timestamp: Long
    )
    @Serializable
    data class Message(
        val id: Int,
        val author: String,
        val message: String,
        val timestamp: Long
    )
    private class DAOMessenger{
        private fun resultRowToMessenger(row: ResultRow) = Messenger(
            author = row[MessengerTable.author],
            message = row[MessengerTable.message],
            timestamp = row[MessengerTable.timestamp]
        )
        private fun resultRowToMessenger2(row: ResultRow) = Message(
            id = row[MessengerTable.id],
            author = row[MessengerTable.author],
            message = row[MessengerTable.message],
            timestamp = row[MessengerTable.timestamp]
        )
        fun addNewData(data: Messenger){
            transaction(DatabaseSingletonMessenger.database) {
                MessengerTable.insert {
                    it[author] = data.author
                    it[message] = data.message
                    it[timestamp] = data.timestamp
                }
            }

        }
        fun deleteAll(){
            transaction(DatabaseSingletonMessenger.database) {
                MessengerTable.deleteAll()
            }
        }
        fun editData(id: Int, data: Messenger){
            transaction(DatabaseSingletonMessenger.database) {
                MessengerTable.update({MessengerTable.id eq id}){
                    it[author] = data.author
                    it[message] = data.message
                    it[timestamp] = data.timestamp
                }
            }
        }
        fun deleteData(id: Int){
            transaction(DatabaseSingletonMessenger.database) {
                MessengerTable.update({MessengerTable.id eq id}) {
                    it[author] = "[NULL]"
                    it[message] = "[DELETED]"
                }
            }
        }
        fun getMax(): Int{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction MessengerTable
                    .selectAll()
                    .toList()
                    .size
            }
        }
        fun getMessage(index: Int): Message{
            return transaction(DatabaseSingletonMessenger.database) {
                val message = MessengerTable
                    .selectAll()
                    .where { MessengerTable.id eq index }
                    .map(::resultRowToMessenger2)
                    .singleOrNull()!!
                return@transaction Message(
                    id = message.id,
                    author = message.author,
                    message = message.message,
                    timestamp = message.timestamp
                )
            }
        }
        fun getRangeMessages(startIndex: Int, endIndex: Int): List<Message>{
            val arrayList = ArrayList<Message>()
            for (i in startIndex..endIndex){
                arrayList.add(getMessage(i))
            }
            return arrayList.toList()
        }
    }
    private val dao = DAOMessenger()
    fun insertMessage(author: String, message: String, timestamp: Long){
        dao.addNewData(Messenger(author, message, timestamp))
    }
    fun deleteMessage(id: Int){
        dao.deleteData(id)
    }
    fun editData(id: Int, data: Messenger){
        dao.editData(id, data)
    }
    fun getLengthMessages(): Int{
        return try {
            dao.getMax()
        } catch (e: Exception){
            0
        }
    }
    fun getRangeMessage(startIndex: Int, endIndex: Int): List<Message>{
        return try {
            dao.getRangeMessages(startIndex, endIndex)
        } catch (e: Exception){
            listOf()
        }
    }
}