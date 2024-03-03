package ru.anotherworld.features.vk

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureVkRouting() {
    routing {
        post("/vk"){
            val vkController = VkController()
            vkController.getNews(call)
        }
    }
}