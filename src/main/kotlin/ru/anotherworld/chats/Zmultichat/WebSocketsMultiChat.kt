package ru.anotherworld.chats.zmultichat

import io.ktor.client.engine.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.anotherworld.globalPath
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


//PTP -> e_chat1x2; MultiChat -> e_chatMC${UUID}
fun Application.configureWebSocketsMultiChat() {
    val database = MainDatabase2()
    val tokenDatabase = TokenDatabase2()

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/echat2") {
            val nameDB = call.parameters["namedbY"]
            val token = call.parameters["tokenY"]
            if(nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                val login = tokenDatabase.getLoginByToken(token)
                val blacklist = controller.getAllBlacklist()
                val thisConnection = Connection(this, nameDB)
                connections += thisConnection
                try {
                    for(frame in incoming){
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readBytes().decodeToString()
                        val data = Json.decodeFromString<DataMessengerEncrypted>(receivedText)
                        var ready = true
                        connections.forEach {
                            if(nameDB == it.nameDB && login !in blacklist){
                                it.session.send(
                                    Json.encodeToString<DataMessengerEncrypted>(
                                        DataMessengerEncrypted(
                                            id = data.id,
                                            author = data.author,
                                            encText = data.encText,
                                            time = data.time,
                                            sendTo = data.sendTo
                                        )
                                    )
                                )
                                if (ready){
                                    ready = false
                                    controller.newMessage(data)
                                }
                            }
//                            else if(database.getPrivacy(data.author)){
//                                it.session.send(
//                                    Json.encodeToString<DataMessengerEncrypted>(
//                                        DataMessengerEncrypted(
//                                            id = data.id,
//                                            author = "NULL",
//                                            encText = """{"privacy": false}""",
//                                            time = data.time,
//                                            sendTo = data.sendTo
//                                        )
//                                    )
//                                )
//                            }
                        }
                    }
                } catch (e: Exception){
                    println(e)
                } finally {
                    connections -= thisConnection
                }
            }
        }
        get("/ecount"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if (nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                if (tokenDatabase.getLoginByToken(token) !in controller.getAllBlacklist()){
                    call.respond<CountMessages>(HttpStatusCode.OK, CountMessages(controller.getCurrentCountMessages()))
                }
                else call.respond(HttpStatusCode.Conflict, "You are in blacklist!")
            }
        }
        post("/echatdelete"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if(token == null || nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val login = tokenDatabase.getLoginByToken(token)
                val controller = MultiChatController(nameDB)
                val access = controller.getAccessRights(login)
                if((access != null && access) || (database.getJob(login) == 5) && login !in controller.getAllBlacklist()){
                    val file = File("$globalPath/sqldatabase/e_messenger/$nameDB.mv.db")
                    if (file.exists()) file.delete()
                }
            }
        }
        post("/echataccess"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            val qAdmin = call.parameters["qadmin"]
            val newAccess = call.parameters["newaccess"]
            if (nameDB == null || token == null || qAdmin == null || newAccess == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val login = tokenDatabase.getLoginByToken(token)
                val controller = MultiChatController(nameDB)
                val access = controller.getAccessRights(login)
                if (access != null && access && login !in controller.getAllBlacklist()){
                    controller.updateAccessRights(newAccess.toBoolean(), login, qAdmin)
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.Conflict, "You are not admin!")
            }
        }
        get("/echatgetblacklist"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if(token == null || nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                val login = tokenDatabase.getLoginByToken(token)
                if(login in controller.getAllKeys().map { it.login }.toList()
                    && login !in controller.getAllBlacklist()){
                    val list = controller.getAllBlacklist()
                    call.respond<RespondBlacklist>(HttpStatusCode.OK, RespondBlacklist(list))
                }
                else call.respond(HttpStatusCode.Conflict, "You are not in chat!")
            }
        }
        post("/echataddinblacklist"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"] //token users, who request ban
            val guilty = call.parameters["guilty"] //request login of guilty person
            if (nameDB == null || token == null || guilty == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                if ("echat" in nameDB){ //chat on two person, ban has been confirmed
                    if (tokenDatabase.getLoginByToken(token) in controller.getAllKeys().map { it.login }.toList()){
                        controller.addInBlacklist(guilty)
                        call.respond(HttpStatusCode.OK)
                    }
                    else call.respond(HttpStatusCode.BadRequest, "You are not in chat!")
                }
                else{ //ban in multiChat
                    val login = tokenDatabase.getLoginByToken(token)
                    val access = controller.getAccessRights(login)
                    if (login in controller.getAllKeys().map { it.login }.toList()
                        && access != null && access){
                        controller.addInBlacklist(guilty)
                        call.respond(HttpStatusCode.OK)
                    }
                    else call.respond(HttpStatusCode.Conflict, "You are not admin!")
                }
            }
        }
        post("/echatremovefromblacklist"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"] //token users, who request unban
            val guilty = call.parameters["guilty"] //request login of guilty person
            if (nameDB == null || token == null || guilty == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                if("echat" in nameDB){
                    if (tokenDatabase.getLoginByToken(token) in controller.getAllKeys().map { it.login }.toList()){
                        controller.removeFromBlacklist(guilty)
                        call.respond(HttpStatusCode.OK)
                    }
                    else call.respond(HttpStatusCode.BadRequest, "You are not in chat!")
                }
                else{
                    val login = tokenDatabase.getLoginByToken(token)
                    val access = controller.getAccessRights(login)
                    if (login in controller.getAllKeys().map { it.login }.toList()
                        && access != null && access){
                        controller.removeFromBlacklist(guilty)
                        call.respond(HttpStatusCode.OK)
                    }
                    else call.respond(HttpStatusCode.Conflict, "You are not admin!")
                }
            }
        }
        get("/echatgetaccess"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if(nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val login = tokenDatabase.getLoginByToken(token)
                val controller = MultiChatController(nameDB)
                if (login in controller.getAllKeys().map { it.login }.toList()
                    && login !in controller.getAllBlacklist()){
                    val access = controller.getAccessRights(login)
                    if(access != null) call.respond<RespondAccess>(RespondAccess(access))
                    else call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        post("/getemessages"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if (nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val receive = call.receive<Indexes2>()
                val controller = MultiChatController(nameDB)
                try {
                    if (tokenDatabase.getLoginByToken(token) !in controller.getAllBlacklist()){
                        call.respond(HttpStatusCode.OK,
                            controller.getAllMessagesByIds(receive.startIndex, receive.endIndex))
                    }
                    else call.respond(HttpStatusCode.Conflict, "You are in blacklist!")
                } catch (e: Exception){
                    println(e)
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        post("/initnewuser2"){
            val nameDB = call.parameters["namedbZ"]
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val receive = call.receive<InitEncUser>()
                val controller = MultiChatController(nameDB)
                try {
                    if(tokenDatabase.getTokenByLogin(receive.login) == receive.token
                        && receive.login !in controller.getAllBlacklist()){
                        val allUsers = controller.getAllKeys().map{ it.login }.toList()
                        if (receive.login !in allUsers || allUsers.isEmpty()){
                            controller.addNewUser(receive.login, receive.publicKey)
                            call.respond(HttpStatusCode.OK)
                        }
                        else call.respond(HttpStatusCode.Conflict)
                    }
                    else call.respond(HttpStatusCode.BadRequest)
                } catch (e: Exception){
                    println(e)
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        get("/getencusers"){
            val nameDB = call.parameters["namedb6"]
            val token = call.parameters["token6"]
            if(nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller = MultiChatController(nameDB)
                val users = controller.getAllKeys()
                val login = tokenDatabase.getLoginByToken(token)
                if (login !in controller.getAllBlacklist()){
                    var success = false
                    for(data in users){
                        if (login == data.login) {success = true; break;}
                    }
                    if (success){
                        call.respond<List<DataKeys>>(HttpStatusCode.OK, users)
                    }
                    else call.respond(HttpStatusCode.BadRequest)
                }
                else call.respond(HttpStatusCode.Conflict, "You are in blacklist!")
            }
        }
    }
}


@Serializable
data class RespondAccess(
    val access: Boolean
)

@Serializable
data class RespondBlacklist(
    val list: List<String>
)

@Serializable
data class CountMessages(
    val length: Long
)

@Serializable
data class InitEncUser(
    val login: String,
    val token: String,
    val publicKey: String
)

@Serializable
data class Indexes2(
    val startIndex: Long,
    val endIndex: Long
)

class Connection(val session: DefaultWebSocketSession, name: String){
    companion object {
        val lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
    val nameDB = name
}