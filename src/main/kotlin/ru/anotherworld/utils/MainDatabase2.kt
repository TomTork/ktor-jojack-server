package ru.anotherworld.utils

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

data class Article(
    val login: String,
    val password: String,
    val trustLevel: Int,
    val job: Int,
    val privacy: Boolean,
    val icon: String,
    val closedKey: String,
    val openedKey: String,
    val info: String
)

class DAOMainDatabase{
    private fun resultRowToMainDatabase(row: ResultRow) = Article(
        login = row[Articles.login],
        password = row[Articles.password],
        trustLevel = row[Articles.trustLevel],
        job = row[Articles.job],
        privacy = row[Articles.privacy],
        icon = row[Articles.icon],
        closedKey = row[Articles.closedKey],
        openedKey = row[Articles.openedKey],
        info = row[Articles.info]
    )
    suspend fun searchRegisterUser(login: String): Boolean = dbQuery {
        try {
            if (Articles.selectAll().toList().isNotEmpty()){
                return@dbQuery Articles
                    .selectAll()
                    .where { Articles.login eq login }
                    .map(::resultRowToMainDatabase)
                    .singleOrNull()!!
                    .login
                    .isNotEmpty()
            }
            else return@dbQuery false

        } catch (e: Exception){
            println("EVERYTHING IS FINE, USER NOT FOUND. SO, ERROR: $e")
            return@dbQuery false
        }
    }
    suspend fun equalPassword(login: String, checkedPassword: String): Boolean = dbQuery{
        try {
            val pass = Articles
                .select { Articles.login eq login }
                .map(::resultRowToMainDatabase)
                .singleOrNull()!!
                .password
            return@dbQuery pass == checkedPassword
        } catch (e: Exception){
            return@dbQuery false
        }
    }
    suspend fun allMainTables(): List<Article> = dbQuery {
        return@dbQuery Articles.selectAll().map(::resultRowToMainDatabase)
    }
    suspend fun getMainDatabase(login: String): Article? = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()
    }
    suspend fun getInfo(login: String): String = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .info
    }
    suspend fun setPrivacy(login: String, value: Boolean): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[privacy] = value
        } > 0
    }
    suspend fun getPrivacy(login: String): Boolean = dbQuery {
        return@dbQuery Articles
            .selectAll()
            .where { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .privacy
    }
    suspend fun getIcon(login: String): String = dbQuery {
        return@dbQuery Articles
            .selectAll()
            .where { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .icon
    }
    suspend fun setIcon(login: String, value: String): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[icon] = value
        } > 0
    }
    suspend fun setInfo(login: String, value: String): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.info] = value
        } > 0
    }
    suspend fun getPassword(login: String): String = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .password
    }
    suspend fun setPassword(login: String, value: String): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.password] = value
        } > 0
    }
    suspend fun getTrustLevel(login: String): Int = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .trustLevel
    }
    suspend fun setTrustLevel(login: String, value: Int): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.trustLevel] = value
        } > 0
    }
    suspend fun getJob(login: String): Int = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .job
    }
    suspend fun setJob(login: String, value: Int): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.job] = value
        } > 0
    }
    suspend fun getClosedKey(login: String): String = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .closedKey
    }
    suspend fun setClosedKey(login: String, value: String): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.closedKey] = value
        } > 0
    }
    suspend fun getOpenedKey(login: String): String = dbQuery {
        return@dbQuery Articles
            .select { Articles.login eq login }
            .map(::resultRowToMainDatabase)
            .singleOrNull()!!
            .openedKey
    }
    suspend fun setOpenedKey(login: String, value: String): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq login }) {
            it[Articles.openedKey] = value
        } > 0
    }
    suspend fun addNewMainDatabase(data: JoJack): Article? = dbQuery {
        val insertStatement = Articles.insert {
            it[login] = data.login
            it[password] = data.password
            it[trustLevel] = data.trustLevel
            it[job] = data.job
            it[privacy] = data.privacy
            it[icon] = data.icon
            it[closedKey] = data.closedKey
            it[openedKey] = data.openedKey
            it[info] = data.info
        }
        return@dbQuery insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToMainDatabase)
    }
    suspend fun editMainDatabase(data: JoJack): Boolean = dbQuery {
        return@dbQuery Articles.update({ Articles.login eq data.login }) {
            it[password] = data.password
            it[trustLevel] = data.trustLevel
            it[job] = data.job
            it[privacy] = data.privacy
            it[icon] = data.icon
            it[closedKey] = data.closedKey
            it[openedKey] = data.openedKey
            it[info] = data.info
        } > 0
    }
    suspend fun deleteMainDatabase(login: String): Boolean = dbQuery {
        return@dbQuery Articles.deleteWhere { Articles.login eq login } > 0
    }
}

object Articles : Table("main"){
    val login = varchar("login", 64)
    val password = varchar("password", 256)
    val trustLevel = integer("trustLevel")
    val job = integer("job")
    val privacy = bool("privacy")
    val icon = varchar("icon", 256)
    val closedKey = varchar("closedKey", 2048)
    val openedKey = varchar("openedKey", 2048)
    val info = varchar("info", 256)

    override val primaryKey = PrimaryKey(login)
}

private val dao = DAOMainDatabase()

class MainDatabase2{
    fun insertAll(data: JoJack){
        runBlocking {
            dao.addNewMainDatabase(data)
        }
    }
    fun getInfo(login: String): String{
        return runBlocking {
            return@runBlocking dao.getInfo(login)
        }
    }
    fun setInfo(login: String, value: String): Boolean{
        return runBlocking {
            return@runBlocking dao.setInfo(login, value)
        }
    }
    fun getPassword(login: String): String{
        return runBlocking {
            return@runBlocking dao.getPassword(login)
        }
    }
    fun setPassword(login: String, password: String): Boolean{
        return runBlocking {
            return@runBlocking dao.setPassword(login, password)
        }
    }
    fun getPrivacy(login: String): Boolean{
        return runBlocking {
            return@runBlocking dao.getPrivacy(login)
        }
    }
    fun setPrivacy(login: String, value: Boolean): Boolean{
        return runBlocking {
            return@runBlocking dao.setPrivacy(login, value)
        }
    }
    fun getIcon(login: String): String{
        return runBlocking {
            return@runBlocking dao.getIcon(login)
        }
    }
    fun setIcon(login: String, value: String): Boolean{
        return runBlocking {
            return@runBlocking dao.setIcon(login, value)
        }
    }
    fun getTrustLevel(login: String): Int{
        return runBlocking {
            return@runBlocking dao.getTrustLevel(login)
        }
    }
    fun setTrustLevel(login: String, value: Int): Boolean{
        return runBlocking {
            return@runBlocking dao.setTrustLevel(login, value)
        }
    }
    fun getJob(login: String): Int{
        return runBlocking {
            return@runBlocking dao.getJob(login)
        }
    }
    fun setJob(login: String, value: Int): Boolean{
        return runBlocking {
            return@runBlocking dao.setJob(login, value)
        }
    }
    fun getClosedKey(login: String): String{
        return runBlocking {
            return@runBlocking dao.getClosedKey(login)
        }
    }
    fun setClosedKey(login: String, value: String): Boolean{
        return runBlocking {
            return@runBlocking dao.setClosedKey(login, value)
        }
    }
    fun getOpenedKey(login: String): String{
        return runBlocking {
            return@runBlocking dao.getOpenedKey(login)
        }
    }
    fun setOpenedKey(login: String, value: String): Boolean{
        return runBlocking {
            return@runBlocking dao.setOpenedKey(login, value)
        }
    }
    fun searchRegisterUser(login: String): Boolean{
        return runBlocking {
            return@runBlocking dao.searchRegisterUser(login)
        }
    }
    fun equalPassword(login: String, checkedPassword: String): Boolean{
        return runBlocking {
            return@runBlocking dao.equalPassword(login, checkedPassword)
        }
    }
}

data class JoJack(
    val login: String,
    val password: String,
    val trustLevel: Int,
    val job: Int,
    val privacy: Boolean,
    val icon: String,
    val closedKey: String,
    val openedKey: String,
    val info: String
)