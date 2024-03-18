package ru.anotherworld.chats.zmultichat

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import ru.anotherworld.globalPath
import java.sql.SQLException

@Serializable
data class DataHelper(val countMessages: Long)
@Serializable
data class DataKeys(val login: String, val publicKey: String)
@Serializable
data class DataMessengerEncrypted(
    val id: Long,
    val author: String,
    val encText: String,
    val time: Long,
    val sendTo: String
)

private lateinit var name: String

class MultiChatController(nameDB: String) {
    init {
        name = nameDB
    }
    private object MessengerEncryptedTable : Table("emessenger"){
        val autoId = integer("autoid").autoIncrement()
        val id = long("id")
        val author = varchar("author", 128)
        val encText = varchar("encText", 131072)
        val time = long("time")
        val sendTo = varchar("sendto", 128)

        override val primaryKey = PrimaryKey(autoId)
    }
    private object Keys : Table("keys"){
        val id = integer("id").autoIncrement()

        val login = varchar("login", 128)
        val publicKey = varchar("key", 4096)

        override val primaryKey = PrimaryKey(id)
    }
    private object Helper : Table("helper"){
        val id = integer("id").autoIncrement()

        val countMessages = long("count")

        override val primaryKey = PrimaryKey(id)
    }

    object DatabaseSingletonMessenger{
        var database: Database? = null
        private fun createHikariDataSource(
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
                    url = "jdbc:h2:$globalPath/sqldatabase/e_messenger/${name};DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                    driver = "org.h2.Driver"
                )
            )
            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(tables = arrayOf(MessengerEncryptedTable, Keys, Helper))
            }
        }
    }
    init {
        DatabaseSingletonMessenger.init()
    }
    private fun resultToHelper(row: ResultRow) = DataHelper(
        countMessages = row[Helper.countMessages]
    )
    private fun initCountMessages(){
        transaction(DatabaseSingletonMessenger.database) {
            val currentCountMessages = Helper
                .selectAll()
                .where { Helper.id eq 1 }
                .map(::resultToHelper)
                .singleOrNull()
            if (currentCountMessages == null){
                Helper.insert {
                    it[countMessages] = 0
                }
            }
        }
    }
    init {
        initCountMessages() //Этот параметр должен быть заполнен, чтобы не возникала ошибка
    }
    private class DAOHelper{
        private fun resultToHelper(row: ResultRow) = DataHelper(
            countMessages = row[Helper.countMessages]
        )
        fun getCurrentCountMessages(): Long{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction Helper
                    .selectAll()
                    .where { Helper.id eq 1 }
                    .map(::resultToHelper)
                    .singleOrNull()
                    ?.countMessages ?: throw SQLException("TT, ERROR IN ::MULTICHAT. CountMessages is NULL!")
            }
        }
        fun addInCounterNewValue(){
            transaction(DatabaseSingletonMessenger.database) {
                Helper.update({ Helper.id eq 1 }) {
                    it[countMessages] = getCurrentCountMessages() + 1
                }
            }
        }
        fun removeInCounterOldValue(){ //Experimental, do not use this
            transaction(DatabaseSingletonMessenger.database) {
                Helper.update({ Helper.id eq 1 }) {
                    it[countMessages] = getCurrentCountMessages() - 1
                }
            }
        }
    }
    private val daoHelper = DAOHelper()
    fun getCurrentCountMessages(): Long = daoHelper.getCurrentCountMessages()
    private class DAOKeys{
        private fun resultToKeys(row: ResultRow) = DataKeys(
            login = row[Keys.login],
            publicKey = row[Keys.publicKey]
        )
        fun addNewUser(login: String, publicKey: String){
            transaction(DatabaseSingletonMessenger.database) {
                Keys
                    .insert {
                        it[Keys.login] = login
                        it[Keys.publicKey] = publicKey
                }
            }
        }
        fun getAll(): List<DataKeys>{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction Keys
                    .selectAll()
                    .map(::resultToKeys)
                    .toList()
            }
        }
        fun getKeyByLogin(login: String): String?{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction Keys
                    .selectAll()
                    .where { Keys.login eq login }
                    .map(::resultToKeys)
                    .singleOrNull()
                    ?.publicKey
            }
        }
        fun getLoginByKey(key: String): String?{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction Keys
                    .selectAll()
                    .where { Keys.publicKey eq key }
                    .map(::resultToKeys)
                    .singleOrNull()
                    ?.login
            }
        }
    }
    private val daoKeys = DAOKeys()
    fun getLoginByKey(key: String): String? = daoKeys.getLoginByKey(key)
    fun getKeyByLogin(login: String): String? = daoKeys.getKeyByLogin(login)
    fun getAllKeys(): List<DataKeys> = daoKeys.getAll()
    fun addNewUser(login: String, publicKey: String) = daoKeys.addNewUser(login, publicKey)
    private class DAOMessenger{
        private fun resultToMessenger(row: ResultRow) = DataMessengerEncrypted(
            id = row[MessengerEncryptedTable.id],
            author = row[MessengerEncryptedTable.author],
            encText = row[MessengerEncryptedTable.encText],
            time = row[MessengerEncryptedTable.time],
            sendTo = row[MessengerEncryptedTable.sendTo]
        )
        fun getAllMessages(): List<DataMessengerEncrypted>{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction MessengerEncryptedTable
                    .selectAll()
                    .map(::resultToMessenger)
                    .toList()
            }
        }
        fun getAllMessagesById(id: Long): List<DataMessengerEncrypted>{
            return transaction(DatabaseSingletonMessenger.database) {
                return@transaction MessengerEncryptedTable
                    .selectAll()
                    .where { MessengerEncryptedTable.id eq id }
                    .map(::resultToMessenger)
                    .toList()
            }
        }
        fun newMessage(data: DataMessengerEncrypted){
            transaction(DatabaseSingletonMessenger.database) {
                MessengerEncryptedTable
                    .insert {
                        it[id] = data.id
                        it[author] = data.author
                        it[encText] = data.encText
                        it[time] = data.time
                        it[sendTo] = data.sendTo
                    }
                DAOHelper().addInCounterNewValue()
            }
        }
        fun updateMessage(data: DataMessengerEncrypted){ //Warning! It is probably raise exception
            transaction(DatabaseSingletonMessenger.database) {
                MessengerEncryptedTable
                    .update({ (MessengerEncryptedTable.id eq data.id) and (MessengerEncryptedTable.author eq data.author) }) {
                        it[id] = data.id
                        it[author] = data.author
                        it[encText] = data.encText
                        it[time] = data.time
                        it[sendTo] = data.sendTo
                    }
            }
        }
        fun deleteMessageById(id: Long){ //Warning! Probably all data via id not been deleted
            transaction(DatabaseSingletonMessenger.database) {
                MessengerEncryptedTable
                    .deleteWhere { MessengerEncryptedTable.id eq id }
            }
            DAOHelper().removeInCounterOldValue()
        }
    }
    private val daoMessenger = DAOMessenger()
    fun getAllMessages(): List<DataMessengerEncrypted> = daoMessenger.getAllMessages()
    fun getAllMessagesById(id: Long): List<DataMessengerEncrypted> = daoMessenger.getAllMessagesById(id)
    fun newMessage(data: DataMessengerEncrypted) = daoMessenger.newMessage(data)
    fun updateMessage(data: DataMessengerEncrypted) = daoMessenger.updateMessage(data)
    fun deleteMessageById(id: Long) = daoMessenger.deleteMessageById(id)
    fun getAllMessagesByIds(startId: Long, endId: Long): List<DataMessengerEncrypted>{
        val array = ArrayList<DataMessengerEncrypted>()
        for(i in startId..endId){
            for(element in getAllMessagesById(i)){
                array.add(element)
            }
        }
        return array.toList()
    }
}