package ru.anotherworld.features.vk

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.anotherworld.utils.VkIdDatabase
import ru.anotherworld.utils.VkPostDatabase2
import java.io.File
import java.io.FileInputStream

class VkController {
    private val vkIdDatabase = VkIdDatabase()
    private val vkPostDatabase = VkPostDatabase2()
    suspend fun getNews(call: ApplicationCall){
        val getPostReceiveRemote = call.receive<GetPost>()
        call.respond(vkPostDatabase.getRangeTextPosts(getPostReceiveRemote.startIndex, getPostReceiveRemote.endIndex))
    }

}