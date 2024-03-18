package ru.anotherworld.chats.zmultichat

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
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


//PTP -> e_chat1x2; MultiChat -> e_chatMC${UUID}
var controller: MultiChatController? = null
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
                controller = MultiChatController(nameDB)
                val thisConnection = Connection(this, nameDB)
                connections += thisConnection
                try {
                    for(frame in incoming){
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readBytes().decodeToString()
                        val data = Json.decodeFromString<DataMessengerEncrypted>(receivedText)
                        var ready = true
                        connections.forEach {
                            println("WATCH-> $nameDB ${it.nameDB}")
                            if(nameDB == it.nameDB){
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
                                println("SENDED")
                                if (ready){
                                    ready = false
                                    controller!!.newMessage(data)
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
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                controller = MultiChatController(nameDB)
                call.respond<CountMessages>(HttpStatusCode.OK, CountMessages(controller!!.getCurrentCountMessages()))
            }
        }
        post("/getemessages"){
            val nameDB = call.parameters["namedb"]
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val receive = call.receive<Indexes2>()
                controller = MultiChatController(nameDB)
                try {
                    call.respond(HttpStatusCode.OK,
                        controller!!.getAllMessagesByIds(receive.startIndex, receive.endIndex))
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
                controller = MultiChatController(nameDB)
                try {
                    if(tokenDatabase.getTokenByLogin(receive.login) == receive.token){
                        val allUsers = controller!!.getAllKeys().map{ it.login }.toList()
                        println("INFO!!! -> ${allUsers}")
                        if (receive.login !in allUsers || allUsers.isEmpty()){
                            controller!!.addNewUser(receive.login, receive.publicKey)
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
                controller = MultiChatController(nameDB)
                val users = controller!!.getAllKeys()
                val login = tokenDatabase.getLoginByToken(token)
                var success = false
                for(data in users){
                    if (login == data.login) {success = true; break;}
                }
                if (success){
                    call.respond<List<DataKeys>>(HttpStatusCode.OK, users)
                }
                else call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

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