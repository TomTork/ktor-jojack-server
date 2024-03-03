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
import java.io.File
import kotlin.concurrent.thread

object VkPostTable : Table("vkposts"){
    val id = integer("id").autoIncrement()

    val iconUrl = varchar("iconUrl", 512)
    val nameGroup = varchar("nameGroup", 70)
    val textPost = varchar("textPost", 4096)
    val imagesUrls = varchar("imagesUrls", 8192)
    val like = integer("l1ke")
    val commentsUrl = varchar("commentsUrl", 128)
    val originalUrl = varchar("originalUrl", 128)
    val dateTime = varchar("dateTime", 19)
    val exclusive = integer("exclusive")
    val reposted = integer("reposted")
    val origName = varchar("origName", 70)
    val origPost = varchar("origPost", 60)

    override val primaryKey = PrimaryKey(id)
}

var database: Database? = null

object DatabaseSingletonVkPostDatabase{
    private fun createHikariDataSource(
        url: String,
        driver: String
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        maximumPoolSize = 4
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })
//    private val database = Database.connect("jdbc:h2:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/database;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
//        driver = "org.h2.Driver")
    fun init(){
        database = Database.connect(createHikariDataSource(url = "jdbc:h2:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/database;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
            driver = "org.h2.Driver"))
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(tables = arrayOf(VkPostTable, TokenTable, LikeTable, Articles))
        }

    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO, db = database) { block() }

class DAOVkPostDatabase{
    private fun resultRowToVkPostDatabase(row: ResultRow) = VkPostData(
        iconUrl = row[VkPostTable.iconUrl],
        nameGroup = row[VkPostTable.nameGroup],
        textPost = row[VkPostTable.textPost],
        imagesUrls = row[VkPostTable.imagesUrls],
        like = row[VkPostTable.like],
        commentsUrl = row[VkPostTable.commentsUrl],
        originalUrl = row[VkPostTable.originalUrl],
        dateTime = row[VkPostTable.dateTime],
        exclusive = row[VkPostTable.exclusive],
        reposted = row[VkPostTable.reposted],
        origName = row[VkPostTable.origName],
        origPost = row[VkPostTable.origPost]
    )
    suspend fun addNewVkPostDatabase(data: VkPostData) = dbQuery{
        VkPostTable.insert {
            it[iconUrl] = data.iconUrl
            it[nameGroup] = data.nameGroup
            it[textPost] = data.textPost
            it[imagesUrls] = data.imagesUrls
            it[like] = data.like
            it[commentsUrl] = data.commentsUrl
            it[originalUrl] = data.originalUrl
            it[dateTime] = data.dateTime
            it[exclusive] = data.exclusive
            it[reposted] = data.reposted
            it[origName] = data.origName
            it[origPost] = data.origPost
        }
//        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToVkPostDatabase)
    }
    suspend fun deleteAll() = dbQuery {
        VkPostTable.deleteAll()
    }
    suspend fun editVkPostDatabase(data: VkPostData2): Boolean = dbQuery {
        VkPostTable.update({ VkPostTable.id eq data.id }) {
            it[iconUrl] = data.iconUrl
            it[nameGroup] = data.nameGroup
            it[textPost] = data.textPost
            it[imagesUrls] = data.imagesUrls
            it[like] = data.like
            it[commentsUrl] = data.commentsUrl
            it[originalUrl] = data.originalUrl
            it[dateTime] = data.dateTime
            it[exclusive] = data.exclusive
            it[reposted] = data.reposted
            it[origName] = data.origName
            it[origPost] = data.origPost
        } > 0
    }
    suspend fun deleteVkPostDatabase(id: Int): Boolean = dbQuery {
        VkPostTable.deleteWhere { VkPostTable.id eq id } > 0
    }
    suspend fun getMaxId(): Int = dbQuery{
        return@dbQuery VkPostTable.selectAll().toList().size
    }
    suspend fun getLikeByOriginalUrl(originalUrl: String): Int = dbQuery {
        return@dbQuery VkPostTable
            .select { VkPostTable.originalUrl eq originalUrl }
            .map(::resultRowToVkPostDatabase)
            .singleOrNull()!!
            .like
    }
    suspend fun newLikeByOriginalUrl(originalUrl: String, id: String, newValue: Int = 1): Boolean =
        dbQuery{
            VkPostTable
                .update({ VkPostTable.originalUrl eq originalUrl }){
                   it[VkPostTable.like] = newValue
                } > 0
        }
    suspend fun deleteLikeByOriginalUrl(originalUrl: String, id: String, newValue: Int = 0): Boolean =
        dbQuery {
            VkPostTable
                .update({ VkPostTable.originalUrl eq originalUrl }) {
                    it[VkPostTable.like] = newValue
                } > 0
        }
    private suspend fun getPost(id: Int): Post =
        dbQuery{
        val query = VkPostTable
            .select { VkPostTable.id eq id }
            .map(::resultRowToVkPostDatabase)
            .singleOrNull()!!
        return@dbQuery Post(
            iconUrl = query.iconUrl,
            groupName = query.nameGroup,
            textPost = query.textPost,
            imagesUrls = Json.decodeFromString<VkImageAndVideo>(query.imagesUrls),
            like = query.like,
            commentsUrl = query.commentsUrl,
            originalUrl = query.originalUrl,
            exclusive = intToBool(query.exclusive),
            reposted = intToBool(query.reposted),
            origName = query.origName,
            origPost = query.origPost
        )
    }
    suspend fun getRangeTextPosts(startIndex: Int, endIndex: Int): GetRPost{
        val listPost = ArrayList<Post>()
        for(index in startIndex..endIndex)listPost.add(getPost(index))
        listPost.reverse()
        return GetRPost(listPost)
    }
}

private val dao = DAOVkPostDatabase()

class VkPostDatabase2{
    private val likesDatabase = LikesDatabase2()
    fun insertAll(data: VkPostData){
        try {
            runBlocking {
                dao.addNewVkPostDatabase(data)
            }
        } catch (e: Exception){
            println(e)
            println(getMaxId())
        }
    }
    fun deleteAll(){
        runBlocking {
            dao.deleteAll()
        }
    }
    fun getMaxId(): Int{
        return runBlocking {
            return@runBlocking dao.getMaxId()
        }
    }
    fun newLikeByOriginalUrl(originalUrl: String, id: String): Boolean{
        return runBlocking {
            likesDatabase.addL1kez(originalUrl, id)
            return@runBlocking dao.newLikeByOriginalUrl(originalUrl, id, newValue = 1)
        }
    }
    fun deleteLikeByOriginalUrl(originalUrl: String, id: String): Boolean{
        return runBlocking {
            likesDatabase.removeL1kez(originalUrl, id)
            return@runBlocking dao.deleteLikeByOriginalUrl(originalUrl, id, newValue = 0)
        }
    }
    fun getRangeTextPosts(startIndex: Int, endIndex: Int): GetRPost{
        return runBlocking {
            return@runBlocking dao.getRangeTextPosts(startIndex, endIndex)
        }
    }
    fun getLikeByOriginalUrl(originalUrl: String): Int{
        return runBlocking {
            return@runBlocking dao.getLikeByOriginalUrl(originalUrl)
        }
    }
}

//class VkPostDatabase2 {
//    private val likesDatabase = LikesDatabase2()
//    private val database = Database.connect("jdbc:h2:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/VkDatabase/VkPostDatabase2",
//        "org.h2.Driver")
//    object VkPostTable : Table(){
//        val id = integer("id").autoIncrement()
//
//        val iconUrl = varchar("iconUrl", 256)
//        val nameGroup = varchar("nameGroup", 70)
//        val textPost = varchar("textPost", 4096)
//        val imagesUrls = varchar("imagesUrls", 8192)
//        val like = integer("l1ke")
//        val commentsUrl = varchar("commentsUrl", 128)
//        val originalUrl = varchar("originalUrl", 128)
//        val dateTime = varchar("dateTime", 19)
//        val exclusive = integer("exclusive")
//        val reposted = integer("reposted")
//        val origName = varchar("origName", 70)
//        val origPost = varchar("origPost", 60)
//
//        override val primaryKey = PrimaryKey(id, name = "id")
//    }
//    init {
//        if(!File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/VkDatabase/VkPostDatabase2.mv.db").exists()){
//            transaction(database) {
//                SchemaUtils.create(VkPostTable)
//            }
//        }
//    }
//    fun getMaxId(): Int{
//        return transaction(database) {
//            return@transaction VkPostTable.selectAll().toList().size
//        }
//    }
//    private fun getLikeByOriginalUrl(originalUrl: String): Int{
//        return transaction(database) {
//            return@transaction VkPostTable.select { VkPostTable.originalUrl eq originalUrl }.first()[VkPostTable.like]
//        }
//    }
//    fun newLikeByOriginalUrl(originalUrl: String, id: String){
//        val like = getLikeByOriginalUrl(originalUrl)
//        transaction(database) {
//            VkPostTable.update({VkPostTable.originalUrl eq originalUrl}) { it[VkPostTable.like] = like + 1 }
//        }
//        likesDatabase.addL1kez(originalUrl, id)
//    }
//    fun deleteLikeByOriginalUrl(originalUrl: String, id: String){
//        val like = getLikeByOriginalUrl(originalUrl)
//        if (like > 0){
//            transaction(database) {
//                VkPostTable.update({VkPostTable.originalUrl eq originalUrl}) { it[VkPostTable.like] = like - 1 }
//            }
//        }
//        likesDatabase.removeL1kez(originalUrl, id)
//    }
//    private fun getPost(id: Int): Post{
//        return transaction(database) {
//            val query = VkPostTable.select { VkPostTable.id eq id }.first()
//            return@transaction Post(
//                iconUrl = query[VkPostTable.iconUrl],
//                groupName = query[VkPostTable.nameGroup],
//                textPost = query[VkPostTable.textPost],
//                imagesUrls = Json.decodeFromString<VkImageAndVideo>(query[VkPostTable.imagesUrls]),
//                like = query[VkPostTable.like],
//                commentsUrl = query[VkPostTable.commentsUrl],
//                originalUrl = query[VkPostTable.originalUrl],
//                exclusive = intToBool(query[VkPostTable.exclusive]),
//                reposted = intToBool(query[VkPostTable.reposted]),
//                origName = query[VkPostTable.origName],
//                origPost = query[VkPostTable.origPost]
//            )
//        }
//    }
//    fun getAllTextPosts(): List<Any>{
//        return transaction(database) {
//            return@transaction VkPostTable.selectAll().toList().map { it[VkPostTable.textPost] }
//        }
//    }
//    fun getRangeTextPosts(startIndex: Int, endIndex: Int): GetRPost{
//        val listPost = ArrayList<Post>()
//        for(index in startIndex..endIndex)listPost.add(getPost(index))
//        listPost.reverse()
//        return GetRPost(listPost)
//    }
//    fun insertAll(data: VkPostData){
//        transaction(database) {
//            VkPostTable.insert {
//                it[iconUrl] = data.iconUrl
//                it[nameGroup] = data.nameGroup
//                it[textPost] = data.textPost
//                it[imagesUrls] = data.imagesUrls
//                it[like] = data.like
//                it[commentsUrl] = data.commentsUrl
//                it[originalUrl] = data.originalUrl
//                it[dateTime] = data.dateTime
//                it[exclusive] = data.exclusive
//                it[reposted] = data.reposted
//                it[origName] = data.origName
//                it[origPost] = data.origPost
//            }
//        }
//    }
//    fun dropTable(){
//        transaction(database) {
//            VkPostTable.deleteAll()
//        }
//    }
//}

data class VkPostData(
    val iconUrl: String,
    val nameGroup: String,
    val textPost: String,
    val imagesUrls: String,
    val like: Int,
    val commentsUrl: String,
    val originalUrl: String,
    val dateTime: String,
    val exclusive: Int,
    val reposted: Int,
    val origName: String,
    val origPost: String
)

data class VkPostData2(
    val id: Int,
    val iconUrl: String,
    val nameGroup: String,
    val textPost: String,
    val imagesUrls: String,
    val like: Int,
    val commentsUrl: String,
    val originalUrl: String,
    val dateTime: String,
    val exclusive: Int,
    val reposted: Int,
    val origName: String,
    val origPost: String
)

private fun intToBool(value: Int): Boolean{
    return value == 1
}