package ru.anotherworld.features.posts

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureLikeListener(){
    routing {
        post("/like") {
            val likeListenerController = LikeListenerController()
            likeListenerController.registerLike(call)
        }
    }
}


