package ru.anotherworld.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.anotherworld.features.vk.GetRPost
import ru.anotherworld.features.vk.Post
import ru.anotherworld.features.vk.VkImageAndVideo
import ru.anotherworld.globalPath
import java.io.File
import kotlin.concurrent.thread

object CommentsTable : Table("comments"){
    val id = integer("id")

    val author = varchar("author", 64)
    val text = varchar("text", 8192)
    val likes = integer("likes")
    val answer = integer("answer")

    override val primaryKey = PrimaryKey(id)
}

class DAOCommentsDatabase{
    private fun resultRowToCommentsDatabase(row: ResultRow) = CommentsData(
        id = row[CommentsTable.id],
        author = row[CommentsTable.author],
        text = row[CommentsTable.text],
        likes = row[CommentsTable.likes],
        answer = row[CommentsTable.answer]
    )
    suspend fun addNewCommentsDatabase(data: CommentsData) = dbQuery {
        CommentsTable.insert {
            it[id] = data.id
            it[author] = data.author
            it[text] = data.text
            it[likes] = data.likes
            it[answer] = data.answer
        }
    }
    suspend fun deleteAll() = dbQuery {
        CommentsTable.deleteAll()
    }
    suspend fun editCommentsDatabase(data: CommentsData): Boolean = dbQuery {
        CommentsTable.update({ CommentsTable.id eq data.id }) {
            it[author] = data.author
            it[text] = data.text
            it[likes] = data.likes
            it[answer] = data.answer
        } > 0
    }
    suspend fun getAllCommentsById(id: Int): List<CommentsData> = dbQuery{
        return@dbQuery CommentsTable
            .selectAll()
            .where { CommentsTable.id eq id }
            .map(::resultRowToCommentsDatabase)
    }
}

private val dao = DAOCommentsDatabase()
class CommentsDatabase{
    fun insertAll(data: CommentsData){
        runBlocking {
            dao.addNewCommentsDatabase(data)
        }
    }
    fun editAll(data: CommentsData){
        runBlocking {
            dao.editCommentsDatabase(data)
        }
    }
    fun getAllCommentsById(id: Int): List<CommentsData>{
        return runBlocking {
            return@runBlocking dao.getAllCommentsById(id)
        }
    }
}

@Serializable
data class CommentsData(
    val id: Int,
    val author: String,
    val text: String,
    val likes: Int,
    val answer: Int
)

@Serializable
data class IncomingCommentsData(
    val token: String,
    val id: Int,
    val author: String,
    val text: String,
    val likes: Int,
    val answer: Int
)