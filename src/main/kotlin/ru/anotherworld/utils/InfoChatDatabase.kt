package ru.anotherworld.utils

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class InfoChat(
    val login: String,
    val urlChat: String,
    val nameChat: String,
    val users: String,
    val iconChat: String
)

@Serializable
data class MyInfoChat(
    val urlChat: String,
    val nameChat: String,
    val users: String,
    val iconChat: String
)

object InfoChatTable : Table("infochat"){
    val login = varchar("login", 64)
    val urlChat = varchar("url", 128)
    val nameChat = varchar("name", 128)
    val users = varchar("users", 32768)
    val icon = varchar("icon", 128)
}

class DAOInfoChatDatabase{
    private fun resultRowToInfoChat(row: ResultRow) = InfoChat(
        login = row[InfoChatTable.login],
        urlChat = row[InfoChatTable.urlChat],
        nameChat = row[InfoChatTable.nameChat],
        users = row[InfoChatTable.users],
        iconChat = row[InfoChatTable.icon]
    )
    private fun resultRowToExport(row: ResultRow) = MyInfoChat(
        urlChat = row[InfoChatTable.urlChat],
        nameChat = row[InfoChatTable.nameChat],
        users = row[InfoChatTable.users],
        iconChat = row[InfoChatTable.icon]
    )
    suspend fun getAll(login: String): List<MyInfoChat> = dbQuery{
        return@dbQuery InfoChatTable
            .selectAll()
            .where { InfoChatTable.login eq login }
            .map(::resultRowToExport)
            .toList()
    }
    suspend fun getAllUsers(): List<InfoChat> = dbQuery {
        return@dbQuery InfoChatTable
            .selectAll()
            .map(::resultRowToInfoChat)
            .toList()
    }
    suspend fun addNewData(data: InfoChat) = dbQuery{
        InfoChatTable
            .insert {
                it[login] = data.login
                it[urlChat] = data.urlChat
                it[nameChat] = data.nameChat
                it[users] = data.users
                it[icon] = data.iconChat
            }
    }
    suspend fun editData(data: InfoChat) = dbQuery {
        InfoChatTable
            .update({ InfoChatTable.login eq data.login }) {
                it[urlChat] = data.urlChat
                it[nameChat] = data.nameChat
                it[users] = data.users
                it[icon] = data.iconChat
            }
    }
    suspend fun deleteChatByUrlChatAndLogin(urlChat: String, login: String) = dbQuery {
        InfoChatTable
            .deleteWhere { (InfoChatTable.login eq login) and (InfoChatTable.urlChat eq urlChat) }
    }
    suspend fun deleteAll() = dbQuery {
        InfoChatTable.deleteAll()
    }
}

private val daoInfoChatDatabase = DAOInfoChatDatabase()
class InfoChatDatabase {
    fun getAllUsers(): List<InfoChat>{
        return runBlocking {
            return@runBlocking daoInfoChatDatabase.getAllUsers()
        }
    }
    fun deleteAll(){
        runBlocking {
            daoInfoChatDatabase.deleteAll()
        }
    }
    fun insertAll(data: InfoChat){
        runBlocking {
            daoInfoChatDatabase.addNewData(data)
        }
    }
    fun editData(data: InfoChat){
        runBlocking {
            daoInfoChatDatabase.editData(data)
        }
    }
    fun deleteChatByUrlChatAndLogin(urlChat: String, login: String){
        runBlocking {
            daoInfoChatDatabase.deleteChatByUrlChatAndLogin(urlChat, login)
        }
    }
    fun getAllByLogin(login: String): List<MyInfoChat>{
        return runBlocking {
            return@runBlocking daoInfoChatDatabase.getAll(login)
        }
    }

}