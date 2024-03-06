package ru.anotherworld.features.upload.icons

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.io.File

fun Application.configureIconRouting() {
    routing {
        get("/icon"){
            val login = call.parameters["login4"]
            if (login != null){
                val iconController = IconController()
                val data = iconController.getIcon(call, login)
                if(data != null) call.respondFile(data)
            }
        }
        post("/set-icon") {
            val response = call.parameters["login3"]
            val token = call.parameters["token3"]
            if(token != null && TokenDatabase2().getLoginByToken(token) == response){
                val file = File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/icons/${response}.png")
                if (file.exists()) file.delete()

                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if(part is PartData.FileItem) {
                        part.streamProvider().use { its ->
                            file.outputStream().buffered().use {
                                its.copyTo(it)
                            }
                        }
                    }
                    part.dispose()
                }
//                val file = File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/icons/${response}.png")
//                call.receiveChannel().copyAndClose(file.writeChannel())
            }
        }
    }
}