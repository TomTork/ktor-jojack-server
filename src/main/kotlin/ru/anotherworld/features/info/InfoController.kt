package ru.anotherworld.features.info

import io.ktor.server.application.*
import io.ktor.server.response.*
import ru.anotherworld.utils.VkPostDatabase2

class InfoController {
    private val postDatabase = VkPostDatabase2()
    suspend fun getInfo(call: ApplicationCall){
        call.respond(InfoRemote(postDatabase.getMaxId()))
    }
}