package ru.anotherworld.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object TokenTable : Table("tokens"){
    val id = integer("id").autoIncrement()
    val login = varchar("login", 64)
    val token = varchar("token", 64)

    override val primaryKey = PrimaryKey(id)
}

class DAOTokensDatabase{
    private fun resultRowToTokensDatabase(row: ResultRow) = TokensA(
        id = row[TokenTable.id],
        login = row[TokenTable.login],
        token = row[TokenTable.token]
    )
    suspend fun addNewTokenDatabase(data: TokensA2): TokensA? = dbQuery {
        val insertStatement = TokenTable.insert {
            it[TokenTable.login] = data.login
            it[TokenTable.token] = data.token
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToTokensDatabase)
    }
    suspend fun editTokenDatabase(data: TokensA): Boolean = dbQuery {
        TokenTable.update({ TokenTable.id eq data.id }) {
            it[TokenTable.login] = data.login
            it[TokenTable.token] = data.token
        } > 0
    }
    suspend fun deleteTokenDatabase(id: Int): Boolean = dbQuery{
        TokenTable.deleteWhere { TokenTable.id eq id } > 0
    }
    suspend fun getLogin(id: Int): String = dbQuery{
        return@dbQuery TokenTable
            .select { TokenTable.id eq id }
            .map(::resultRowToTokensDatabase)
            .singleOrNull()!!
            .login
    }
    suspend fun getIdByLogin(login: String): Int = dbQuery {
        return@dbQuery TokenTable
            .select { TokenTable.login eq login }
            .map(::resultRowToTokensDatabase)
            .singleOrNull()!!
            .id
    }
    suspend fun searchFieldsLogin(q: String): List<Pair<String, String>> = dbQuery{
        val arrayList = ArrayList<Pair<String, String>>()
        var list0 = TokenTable
            .slice(TokenTable.id, TokenTable.login)
            .selectAll()
            .where { TokenTable.login like "%$q%" }
            .map { it.toString().substringAfter("=") }
            .toList()
        var time: List<String>

        if (list0.size > 16) list0 = list0.subList(0, 16)
        for (i in list0.indices){
            time = list0[i].split(", ")
            arrayList.add(Pair(time[1].substringAfter("="), time[0]))
        }
        return@dbQuery arrayList.toList()
    }
    suspend fun searchFieldsId(q: String): List<Pair<String, String>> = dbQuery{
        try{
            val arrayList = ArrayList<Pair<String, String>>()
            var list0 = TokenTable
                .slice(TokenTable.id, TokenTable.login)
                .selectAll()
                .where { TokenTable.id eq q.toInt() }
                .map { it.toString().substringAfter("=") }
                .toList()
            var time: List<String>

            if (list0.size > 16) list0 = list0.subList(0, 16)
            for (i in list0.indices){
                time = list0[i].split(", ")
                arrayList.add(Pair(time[1].substringAfter("="), time[0]))
            }
            return@dbQuery arrayList.toList()
        } catch (e: Exception){
            emptyList()
        }
    }
    suspend fun getIdByToken(token: String): Int = dbQuery {
        return@dbQuery TokenTable
            .select { TokenTable.token eq token }
            .map(::resultRowToTokensDatabase)
            .singleOrNull()!!
            .id
    }
    suspend fun getLoginByToken(token: String): String = dbQuery {
        return@dbQuery TokenTable
            .select { TokenTable.token eq token }
            .map(::resultRowToTokensDatabase)
            .singleOrNull()!!
            .login
    }
    suspend fun getTokenByLogin(login: String): String = dbQuery {
        return@dbQuery TokenTable
            .select { TokenTable.login eq login }
            .map(::resultRowToTokensDatabase)
            .singleOrNull()!!
            .token
    }
}

private val dao = DAOTokensDatabase()

class TokenDatabase2{
    fun searchFieldsLogin(q: String): List<Pair<String, String>>{
        return runBlocking {
            return@runBlocking dao.searchFieldsLogin(q)
        }
    }
    fun searchFieldsId(q: String): List<Pair<String, String>>{
        return runBlocking {
            return@runBlocking dao.searchFieldsId(q)
        }
    }
    fun insertAll(data: TokensA2){
        runBlocking {
            dao.addNewTokenDatabase(data)
        }
    }
    fun getLogin(id: Int): String{
        return runBlocking {
            return@runBlocking dao.getLogin(id)
        }
    }
    fun getIdByLogin(login: String): Int{
        return runBlocking {
            return@runBlocking dao.getIdByLogin(login)
        }
    }
    fun getIdByToken(token: String): Int{
        return runBlocking {
            return@runBlocking dao.getIdByToken(token)
        }
    }
    fun getLoginByToken(token: String): String{
        return runBlocking {
            return@runBlocking dao.getLoginByToken(token)
        }
    }
    fun getTokenByLogin(login: String): String{
        return runBlocking {
            return@runBlocking dao.getTokenByLogin(login)
        }
    }
}

data class TokensA(
    val id: Int,
    val login: String,
    val token: String
)
data class TokensA2(
    val login: String,
    val token: String
)


data class TokensDB(
    val login: String,
    val token: String
)





