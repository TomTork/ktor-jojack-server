package ru.anotherworld.features.upload.icons

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

fun Application.configureIconRouting() {
    routing {
        get("/icon"){
            val iconController = IconController()
            val data = iconController.getIcon(call)
            if(data != null) call.respondFile(data)
            else call.respondText("null")
        }
        post("/set-icon") {
            val response = call.parameters["login"]
            val file = File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/icons/${response}.png")
            call.receiveChannel().copyAndClose(file.writeChannel())
        }
    }
}