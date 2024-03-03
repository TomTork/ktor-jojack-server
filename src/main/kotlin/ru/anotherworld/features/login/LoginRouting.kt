package ru.anotherworld.features.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.anotherworld.cache.InMemoryCache
import ru.anotherworld.cache.TokenCache
import java.util.UUID

fun Application.configureLoginRouting() {
    routing {
        post("/login"){
            val loginController = LoginController()
            loginController.performLogin(call)
        }
    }
}