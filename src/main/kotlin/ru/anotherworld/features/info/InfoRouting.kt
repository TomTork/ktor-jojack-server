package ru.anotherworld.features.info

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureInfoRouting() {
    routing {
        get("info") {
            val infoController = InfoController()
            infoController.getInfo(call)
        }
    }
}