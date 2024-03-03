package ru.anotherworld.chats.room

import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import ru.anotherworld.chats.data.MessageDataSource
import ru.anotherworld.chats.data.model.Message
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.*

class RoomController(
    private val messageDataSource: MessageDataSource
) {
    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(
        username: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if(members.containsKey(username)){
            throw MemberAlreadyExistsException()
        }
        members[username] = Member(username, sessionId, socket)
    }

    suspend fun sendMessage(senderUsername: String, message: String){
        var ready = true
        members.values.forEach { member ->
            val messageEntity = Message(
                message,
                senderUsername,
                System.currentTimeMillis()
            )
            if (ready){
                messageDataSource.insertMessage(messageEntity)
                ready = false
            }
            val parsedMessage = Json.encodeToString(messageEntity)
            member.socket.send(Frame.Text(parsedMessage))

        }
    }

    suspend fun getAllMessages(): List<Message>{
        return messageDataSource.getAllMessages()
    }

    suspend fun tryDisconnect(username: String){
        members[username]?.socket?.close()
        if (members.containsKey(username)){
            members.remove(username)
        }
    }
}