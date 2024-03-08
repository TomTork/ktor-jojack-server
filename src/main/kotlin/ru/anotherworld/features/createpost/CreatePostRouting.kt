package ru.anotherworld.features.createpost

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.anotherworld.features.vk.VkGetPost
import ru.anotherworld.features.vk.VkImageAndVideo
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import ru.anotherworld.utils.VkPostData
import ru.anotherworld.utils.VkPostDatabase2

//Ссылки на изображения обрабатываются в виде s://name.png
fun Application.configureCreatePostRouting() {
    val mainDatabase = MainDatabase2()
    val tokenDatabase = TokenDatabase2()
    val postDatabase = VkPostDatabase2()

    routing {
        post("/create-post"){
            val token = call.parameters["tokenx"]
            if (token != null && mainDatabase.getJob(tokenDatabase.getLoginByToken(token)) >= 1){
                val data = call.receive<InsertData>()
                if(data.author != null){
                    postDatabase.insertAll(
                        VkPostData(
                            iconUrl = data.iconGroup,
                            nameGroup = data.namePost,
                            textPost = data.text,
                            imagesUrls = Json.encodeToString<VkImageAndVideo>(data.imagesAndVideo),
                            like = 0,
                            commentsUrl = "",
                            originalUrl = "",
                            dateTime = VkGetPost().getDate(),
                            exclusive = 1,
                            reposted = 0,
                            origPost = "",
                            origName = ""
                        )
                    )
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.Conflict)
            }
            else call.respond(HttpStatusCode.BadRequest)
        }
    }
}

@Serializable
data class InsertData(
    val author: String?,
    val iconGroup: String,
    val namePost: String,
    val text: String,
    val imagesAndVideo: VkImageAndVideo,
)