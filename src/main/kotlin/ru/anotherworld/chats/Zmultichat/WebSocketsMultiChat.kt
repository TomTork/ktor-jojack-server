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
import ru.anotherworld.chats.two.Connection
import ru.anotherworld.chats.two.Indexes
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.util.*


//PTP -> e_chat1x2; MultiChat -> e_chatMC${UUID}
fun Application.configureWebSocketsMultiChat() {
    val database = MainDatabase2()
    val tokenDatabase = TokenDatabase2()
    routing {
        post("/init_new_user"){
            val nameDB = call.parameters["namedb"]
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val receive = call.receive<InitEncUser>()
                println("VALUES!!!2 -> ${receive.login} ${receive.token}")
                val controller4 = MultiChatController(nameDB)
                try {
                    if(tokenDatabase.getTokenByLogin(receive.login) == receive.token){
                        if (receive.login !in controller4.getAllKeys().map{ it.login }.toList()){
                            controller4.addNewUser(receive.login, receive.publicKey)
                            println("VALUES!!! -> ${receive.login} ${receive.publicKey}")
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                } catch (e: Exception){
                    println(e)
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        var controller: MultiChatController?
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/e_chat") {
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
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

                        connections.forEach {
                            if(nameDB == it.nameDB
                                && (!database.getPrivacy(data.author) || database.getJob(data.author) > 1)){
                                it.session.send(
                                    Json.encodeToString<DataMessengerEncrypted>(
                                        DataMessengerEncrypted(
                                            id = data.id,
                                            author = data.author,
                                            encText = data.encText,
                                            time = data.time
                                        )
                                    )
                                )
                                controller!!.newMessage(data)
                            }
                            else if(database.getPrivacy(data.author)){
                                it.session.send(
                                    Json.encodeToString<DataMessengerEncrypted>(
                                        DataMessengerEncrypted(
                                            id = data.id,
                                            author = "NULL",
                                            encText = """{"privacy": false}""",
                                            time = data.time
                                        )
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception){
                    println(e)
                } finally {
                    connections -= thisConnection
                }
            }
        }
        get("/e_count"){
            val nameDB = call.parameters["namedb"]
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller2 = MultiChatController(nameDB)
                call.respond<CountMessages>(HttpStatusCode.OK, CountMessages(controller2.getCurrentCountMessages()))
            }
        }
        post("/get_e_messages"){
            val nameDB = call.parameters["namedb"]
            if (nameDB == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val receive = call.receive<Indexes2>()
                val controller2 = MultiChatController(nameDB)
                try {
                    call.respond(HttpStatusCode.OK,
                        controller2.getAllMessagesByIds(receive.startIndex, receive.endIndex))
                } catch (e: Exception){
                    println(e)
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        get("/get_enc_users"){
            val nameDB = call.parameters["namedb"]
            val token = call.parameters["token"]
            if(nameDB == null || token == null) call.respond(HttpStatusCode.BadRequest)
            else{
                val controller5 = MultiChatController(nameDB)
                val users = controller5.getAllKeys()
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