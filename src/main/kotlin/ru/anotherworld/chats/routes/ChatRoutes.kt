package ru.anotherworld.chats.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import ru.anotherworld.chats.room.MemberAlreadyExistsException
import ru.anotherworld.chats.room.RoomController
import ru.anotherworld.chats.session.ChatSession

fun Route.chatSocket(roomController: RoomController){
    webSocket("/chat-socket") {
        val session = call.sessions.get<ChatSession>()
        if(session == null){
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, message="No session."))
            return@webSocket
        }
        try {
            roomController.onJoin(session.username, session.sessionId, this)
            incoming.consumeEach { frame ->
                if(frame is Frame.Text){
                    roomController.sendMessage(session.username, frame.readText())
                }
            }
        } catch (e: MemberAlreadyExistsException){
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            roomController.tryDisconnect(session.username)
        }
    }
}

fun Route.getAllMessages(roomController: RoomController){
    get("/messages"){
        call.respond(HttpStatusCode.OK, roomController.getAllMessages())
    }
}

