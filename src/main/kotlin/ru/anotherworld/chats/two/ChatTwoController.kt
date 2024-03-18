package ru.anotherworld.chats.two

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.ktor.ext.get
import ru.anotherworld.RSAKotlin
import ru.anotherworld.chats.session.ChatSession
import ru.anotherworld.features.login.cipher
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet

//Example: chat1x2; chat123x666
@OptIn(DelicateCoroutinesApi::class)
fun Application.configureChatTwoController() {
    val database = MainDatabase2()
    routing {
        var messengerController: MessengerController3? = null
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat2") {
            val nameDB = call.parameters["namedb"] ?: ""
            val token = call.parameters["token2"] ?: ""
            messengerController = MessengerController3(nameDB.substringBefore("?"))

            if(nameDB != "" && token != ""){
                val thisConnection = Connection(this, nameDB)
                connections += thisConnection
                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue

                        val receivedText = frame.readBytes().decodeToString()
                        val data = Json.decodeFromString<TMessage>(receivedText)

                        var ready = true
                        connections.forEach {
                            if (nameDB == it.nameDB
                                && (!database.getPrivacy(data.author) || database.getJob(data.author) > 1)){
                                val time = System.currentTimeMillis()
                                val message = data.message
                                it.session.send(
                                    Json.encodeToString<TMessage>(
                                        TMessage(
                                            id = 0,
                                            author = data.author,
                                            message = message,
                                            timestamp = time
                                        )
                                    )
                                )
                                if(ready){
                                    ready = false
                                    Thread(Runnable {
                                        messengerController!!.insertMessage(data.author,
                                            message, time)
                                    }).start()
                                }
                            }
                            else if(database.getPrivacy(data.author)) it.session.send(Json.encodeToString<TMessage>(
                                TMessage(
                                    id = -1,
                                    author = "NULL",
                                    message = """{"privacy": false}""",
                                    timestamp = System.currentTimeMillis()
                                )
                            ))
                        }
                    }
                } catch (e: Exception){
                    println(e)
                    println(e.localizedMessage)
                } finally {
                    connections -= thisConnection
                }
            }
        }
        get("chatmessages"){ //Число сообщений в чате
            val nameDB = call.parameters["namedb"] ?: ""
            val controller = MessengerController3(nameDB)
            call.respond<GetLengthMessages>(HttpStatusCode.OK,
                GetLengthMessages(controller.getLengthMessages()))
        }
        post("getchat2"){ //Получить сообщения по индексам
            val nameDB = call.parameters["namedb"] ?: ""
            val controller = MessengerController3(nameDB)
            val db = call.receive<Indexes>()
            try {
                call.respond(HttpStatusCode.OK, controller.getRangeMessage(db.startIndex, db.endIndex))
            } catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.BadRequest)
            }
        }

    }
}

class Connection(val session: DefaultWebSocketSession, name: String){
    companion object {
        val lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
    val nameDB = name
}

@Serializable
data class ChatOnJoin(
    val username: String,
    val text: String,
    val nameDB: String,
    val oKey: String = ""
)

@Serializable
data class NameDB(
    val nameDB: String,
    val token: String
)


@Serializable
data class GetLengthMessages(
    val length: Int?
)

@Serializable
data class Indexes(
    val startIndex: Int,
    val endIndex: Int
)


@Serializable
data class TMessage(
    val id: Int,
    val author: String,
    val message: String,
    val timestamp: Long
)
