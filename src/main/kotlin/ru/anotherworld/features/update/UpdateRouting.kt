package ru.anotherworld.features.update

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureUpdateRouting() {
    routing {
        post("update-privacy"){
            val updateController = UpdateController()
            updateController.setPrivacy(call)
        }
    }
}