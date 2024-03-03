package ru.anotherworld.features.register

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRegisterRouting() {
    routing {
        post("/register"){
            val registerController = RegisterController()
            registerController.registerNewUser(call)
//            val receive = call.receive<RegisterReceiveRemote>()
        }
    }
}