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
                        println(data)
                        var ready = true
                        connections.forEach {
                            if (nameDB == it.nameDB){
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
                        }
                    }
                } catch (e: Exception){
                    println(e)
                    println(e.localizedMessage)
                } finally {
                    println("Removing $thisConnection!")
                    connections -= thisConnection
                }
            }
        }
        get("chatmessages"){ //Число сообщений в чате
            val nameDB = call.parameters["namedb"] ?: ""
            val controller = MessengerController3(nameDB)
            call.respond<GetLengthMessages>(HttpStatusCode.OK,
                GetLengthMessages(controller.getLengthMessages()))
            println("DATA -> ${controller.getLengthMessages()}")
        }
        post("getchat2"){ //Получить сообщения по индексам
            val nameDB = call.parameters["namedb"] ?: ""
            val controller = MessengerController3(nameDB)
            val db = call.receive<Indexes>()
            try {
                call.respond(HttpStatusCode.OK, controller.getRangeMessage(db.startIndex, db.endIndex))
                println("DATA -> ${controller.getRangeMessage(db.startIndex, db.endIndex)}")
            } catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.Conflict)
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

private fun insertOpenKey(chat: String, oKey: String, token: String, messengerController: MessengerController2){
//    val tokenDatabase = TokenDatabase2()
//    val numbersChat = chat.substringAfter("chat")
//    val first = numbersChat.substringBefore("x").toInt()
//    val second = numbersChat.substringAfter("x").toInt()
//    val myId = tokenDatabase.getIdByToken(token)
//    val pair = messengerController.getPairOpenKey()
//    when (myId) {
//        first -> { if (pair.op1 != "") messengerController.setOp1(oKey) }
//        second -> { if (pair.op2 != "") messengerController.setOp2(oKey) }
//        else -> throw Exception()
//    }
}

private fun getPublicKeys(chat: String): Pair<String, String>{
    val tokenDatabase = TokenDatabase2()
    val mainDatabase = MainDatabase2()
    val numbersChat = chat.substringAfter("chat").substringBefore("?")
    val first = numbersChat.substringBefore("x").toInt() //Id пользователя
    val second = numbersChat.substringAfter("x").toInt()
    val loginFirst = tokenDatabase.getLogin(first)
    val loginSecond = tokenDatabase.getLogin(second)
    return Pair(mainDatabase.getOpenedKey(loginFirst), mainDatabase.getOpenedKey(loginSecond))
}

@Serializable
data class NameDB2(
    val nameDB: String
)

@Serializable
data class TInitPair(
    val nameDB: String,
    val oKey: String,
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
data class TMessage2(
    val id: Int,
    val author: String,
    val message: String,
    val time: Long,
    val openKey: String
)

