package ru.anotherworld.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.*
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

    val loginAuthor = varchar("author", 64)
    val text = varchar("text", 8192)
    val likes = integer("likes")
    val answer = integer("answer")

    override val primaryKey = PrimaryKey(id)
}

class CommentsDatabase {
}