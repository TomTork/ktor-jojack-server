package ru.anotherworld.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.anotherworld.chats.room.RoomController
import ru.anotherworld.chats.routes.chatSocket
import ru.anotherworld.chats.routes.getAllMessages

fun Application.configureRouting() {
    val roomController by inject<RoomController>()

    install(Routing){
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}
