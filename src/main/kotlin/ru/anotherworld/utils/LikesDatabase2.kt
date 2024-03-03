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

//object DatabaseSingletonLikesDatabase{
////    private val database = Database.connect("jdbc:postgresql:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/likesdatabase;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",)
//    private val database = Database.connect("jdbc:postgresql://localhost:5432/likesdatabase;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
//        user = "postgres", password = "password")
//    fun init(){
//        transaction(database) {
//            SchemaUtils.create(LikeTable)
//        }
//    }
//    suspend fun <T> dbQuery(block: suspend () -> T): T =
//        newSuspendedTransaction(Dispatchers.IO) { block() }
//}

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

//class LikesDatabase21 {
//    private val database = Database.connect("jdbc:h2:C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/L1kes2",
//        "org.h2.Driver")
//    object LikeTable : Table(){
//        val originalUrl = varchar("originalUrl", 128)
//        val l1kez = varchar("l1kez", 4096)
//        val number = integer("number")
//
//        override val primaryKey = PrimaryKey(originalUrl, name = "originalUrl")
//    }
//    init {
//        if(!File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/sqldatabase/L1kes2.mv.db").exists()){
//            transaction(database) {
//                SchemaUtils.create(LikeTable)
//            }
//        }
//    }
//    fun insertAll(originalUrl1: String, l1kez1: String, number1: Int){
//        transaction(database) {
//            LikeTable.insert {
//                it[originalUrl] = originalUrl1
//                it[l1kez] = "{\"tokens\":[\"$l1kez1\"]}"
//                it[number] = number1
//            }
//        }
//    }
//    fun getL1kez(originalUrl1: String): L1kes{
//        return try {
//            transaction(database) {
//                return@transaction Json.decodeFromString<L1kes>(
//                    LikeTable.select{ LikeTable.originalUrl eq originalUrl1 }.first()[LikeTable.l1kez]
//                )
//            }
//        } catch (e: Exception){
//            return L1kes(arrayListOf())
//        }
//    }
//    private fun existsLikeForOriginalUrl(originalUrl1: String): Boolean{
//        return try {
//            transaction(database) {
//                return@transaction LikeTable.select{ LikeTable.originalUrl eq originalUrl1 }.first()[LikeTable.number] >= 0
//            }
//        } catch (e: Exception){
//            return false
//        }
//    }
//    private fun getNumber(originalUrl1: String): Int{
//        return try {
//            transaction(database) {
//                return@transaction LikeTable.select { LikeTable.originalUrl eq originalUrl1 }.first()[LikeTable.number]
//            }
//        } catch (e: Exception){
//            return -1
//        }
//    }
//    fun addL1kez(originalUrl1: String, data: String){
//        if (existsLikeForOriginalUrl(originalUrl1)){
//            try {
//                val l1kez = getL1kez(originalUrl1)
//                l1kez.tokens.add(data)
//
//                transaction(database) {
//                    LikeTable.update({LikeTable.originalUrl eq originalUrl1}) { it[LikeTable.l1kez] = Json.encodeToString(l1kez) }
//                }
//                try {
//                    val num = getNumber(originalUrl1)
//                    transaction(database) {
//                        LikeTable.update({LikeTable.originalUrl eq originalUrl1}) { it[LikeTable.number] = num + 1 }
//                    }
//                } catch (e: Exception){
//                    println(e.message)
//                }
//            } catch (e: Exception){
//                insertAll(originalUrl1, data, 1)
//            }
//        }
//        else{
//            insertAll(originalUrl1, data, 1)
//        }
//    }
//    fun removeL1kez(originalUrl1: String, data: String){
//        if (existsLikeForOriginalUrl(originalUrl1)){
//            try {
//                val l1kez = getL1kez(originalUrl1)
//                if (data in l1kez.tokens){
//                    l1kez.tokens.remove(data)
//                    transaction(database) {
//                        LikeTable.update({LikeTable.originalUrl eq originalUrl1}) { it[LikeTable.l1kez] = Json.encodeToString(l1kez) }
//                    }
//                    try {
//                        val num = getNumber(originalUrl1)
//                        transaction(database) {
//                            LikeTable.update({LikeTable.originalUrl eq originalUrl1}) { it[LikeTable.number] = num - 1 }
//                        }
//                    } catch (e: Exception){
//                        println(e.message)
//                    }
//                }
//            } catch (e: Exception){
//                insertAll(originalUrl1, data, 0)
//            }
//        }
//        else {
//            insertAll(originalUrl1, data, 0)
//        }
//    }
//
//}

@Serializable
data class L1kes(
    val tokens: ArrayList<String>
)