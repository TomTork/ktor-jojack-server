package ru.anotherworld.features.createpost

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.anotherworld.features.vk.VkImageAndVideo
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2

fun Application.configureCreatePostRouting() {
    val mainDatabase = MainDatabase2()
    val tokenDatabase = TokenDatabase2()

    routing {
        post("/create-post"){
            val token = call.parameters["tokenx"]
            if (token != null && mainDatabase.getJob(tokenDatabase.getLoginByToken(token)) >= 1){
                val data = call.receive<InsertData>()
            }
        }
    }
}

@Serializable
data class InsertData(
    val author: String?,
    val namePost: String,
    val text: String,
    val imagesAndVideo: VkImageAndVideo,
)