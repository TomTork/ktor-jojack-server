package ru.anotherworld.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DAOLikesDatabase{
    private fun resultRowToLikesDatabase(row: ResultRow) = Likes(
        originalUrl = row[LikeTable.originalUrl],
        l1kez = row[LikeTable.l1kez],
        number = row[LikeTable.number]
    )
    private suspend fun addNewLikeDatabase(data: Likes): Likes? = dbQuery {
        val insertStatement = LikeTable.insert {
            it[LikeTable.originalUrl] = data.originalUrl
            it[LikeTable.l1kez] = data.l1kez
            it[LikeTable.number] = data.number
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToLikesDatabase)
    }
    suspend fun editLikeDatabase(data: Likes): Boolean = dbQuery{
        LikeTable.update({ LikeTable.originalUrl eq data.originalUrl }) {
            it[LikeTable.l1kez] = data.l1kez
            it[LikeTable.number] = data.number
        } > 0
    }
    suspend fun deleteLikeDatabase(originalUrl: String): Boolean = dbQuery {
        LikeTable.deleteWhere { LikeTable.originalUrl eq originalUrl } > 0
    }
    private suspend fun getL1kez(originalUrl: String): L1kes = dbQuery{
        return@dbQuery Json.decodeFromString<L1kes>(
            LikeTable
                .select { LikeTable.originalUrl eq originalUrl }
                .map(::resultRowToLikesDatabase)
                .singleOrNull()!!
                .l1kez
        )
    }
    private suspend fun existsLikeForOriginalUrl(originalUrl: String): Boolean =
        dbQuery {
        return@dbQuery try {
            LikeTable
                .select { LikeTable.originalUrl eq originalUrl }
                .map(::resultRowToLikesDatabase)
                .singleOrNull()!!
                .number >= 0
        } catch (e: Exception){
            false
        }
    }
    private suspend fun getNumber(originalUrl: String): Int = dbQuery {
        try{
            return@dbQuery LikeTable
                .select { LikeTable.originalUrl eq originalUrl }
                .map(::resultRowToLikesDatabase)
                .singleOrNull()!!
                .number
        } catch (e: Exception){
            return@dbQuery -1
        }
    }
    suspend fun addL1kez(originalUrl: String, data: String) = dbQuery {
        if (existsLikeForOriginalUrl(originalUrl)){
            try{
                val l1kez = getL1kez(originalUrl)
                l1kez.tokens.add(data)

                LikeTable.update({ LikeTable.originalUrl eq originalUrl }) {
                    it[LikeTable.l1kez] = Json.encodeToString(l1kez)
                }

                try {
                    val num = getNumber(originalUrl)
                    LikeTable.update({ LikeTable.originalUrl eq originalUrl}) {
                        it[LikeTable.number] = num + 1
                    }
                } catch (e: Exception){
                    println(e.message)
                }

            }catch (e: Exception){
                addNewLikeDatabase(Likes(originalUrl, data, 1))
                println("EVERYTHING IS FINE: $e")
            }
        }
        else {
            addNewLikeDatabase(Likes(originalUrl, data, 1))
        }
    }
    suspend fun removeL1kez(originalUrl: String, data: String) = dbQuery{
        if (existsLikeForOriginalUrl(originalUrl)){
            try {
                val l1kez = getL1kez(originalUrl)
                if (data in l1kez.tokens){
                    l1kez.tokens.remove(data)
                    LikeTable
                        .update({ LikeTable.originalUrl eq originalUrl }) {
                            it[LikeTable.l1kez] = Json.encodeToString(l1kez)
                        }

                    try {
                        val num = getNumber(originalUrl)
                        LikeTable
                            .update({ LikeTable.originalUrl eq originalUrl }) {
                                it[LikeTable.number] = num - 1
                            }

                    } catch (e: Exception){
                        println(e.message)
                    }
                }
                else { TODO() }
            } catch (e: Exception){
                addNewLikeDatabase(Likes(originalUrl, data, 0))
            }
        }
        else {
            addNewLikeDatabase(Likes(originalUrl, data, 0))
        }
    }

}

object LikeTable : Table("l1kes"){
    val originalUrl = varchar("originalUrl", 128)
    val l1kez = varchar("l1kez", 8192)
    val number = integer("number")

    override val primaryKey = PrimaryKey(originalUrl)
}

data class Likes(
    val originalUrl: String,
    val l1kez: String,
    val number: Int
)

private val dao = DAOLikesDatabase()

class LikesDatabase2{
    fun addL1kez(originalUrl: String, data: String){
        runBlocking {
            dao.addL1kez(originalUrl, data)
        }
    }
    fun removeL1kez(originalUrl: String, data: String){
        runBlocking {
            dao.removeL1kez(originalUrl, data)
        }
    }
}


@Serializable
data class L1kes(
    val tokens: ArrayList<String>
)