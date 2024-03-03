package ru.anotherworld.features.upload.images

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

fun Application.configureImageRouting() {
    routing {
        get("/image"){
            val response = call.receive<ImageRemote>()
            val data = File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/others/${response.name}.png") ?: null
            if(data != null) call.respondFile(data)
            else call.respondText("null")
        }
        post("/add-image"){
            val response = call.parameters["name"]
            val file = File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/others/${response}.png")
            call.receiveChannel().copyAndClose(file.writeChannel())
        }
    }
}