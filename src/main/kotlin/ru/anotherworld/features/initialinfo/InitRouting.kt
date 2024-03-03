package ru.anotherworld.features.initialinfo

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureInitRouting() {
    routing {
        post("initialInfo") {
            val initController = InitController()
            initController.getInitialInfo(call)
        }
    }
}