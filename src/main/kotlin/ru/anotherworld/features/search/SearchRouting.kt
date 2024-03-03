package ru.anotherworld.features.search

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureSearchRouting(){
    routing {
        post("/search"){
            val query = call.receive<String>()
            call.respond(Json.encodeToString<SearchP>(SearchP(searchIdOrName(query))))
        }
    }
}

@Serializable
data class SearchP(
    val arr: List<Pair<String, String>>
)