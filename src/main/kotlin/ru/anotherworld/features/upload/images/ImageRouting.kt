package ru.anotherworld.features.upload.images

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import ru.anotherworld.globalPath
import ru.anotherworld.utils.TokenDatabase2
import java.io.File

fun Application.configureImageRouting() {
    routing {
        get("/image"){
            val name = call.parameters["name"]
            if(name != null){
                val imageController = ImageController()
                val data = imageController.getImage(name)
                if(data != null) call.respondFile(data)
            }
        }
        post("/add-image"){
            val response = call.parameters["name"]
            if(response != null){
                val file = File("$globalPath/images/others/${response}.png")
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
            }
        }
    }
}