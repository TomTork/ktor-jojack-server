package ru.anotherworld.features.terminal

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureTerminalRouting() {
    routing {
        get("/terminal"){
            val terminalController = TerminalController()
            terminalController.processingCommand(call)
        }
    }

}