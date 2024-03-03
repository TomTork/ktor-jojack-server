package ru.anotherworld.features.posts

import io.ktor.server.application.*
import io.ktor.server.request.*
import ru.anotherworld.utils.TokenDatabase2
import ru.anotherworld.utils.VkPostDatabase2

class LikeListenerController {

    suspend fun registerLike(call: ApplicationCall){
        val receive = call.receive<RegisterLike>()
        val tokenDatabase = TokenDatabase2()
        val vkPostDatabase = VkPostDatabase2()
        if(receive.status){ //Поставить лайк
            vkPostDatabase.newLikeByOriginalUrl(receive.url, tokenDatabase.getIdByToken(receive.token).toString())
        }
        else vkPostDatabase.deleteLikeByOriginalUrl(receive.url, tokenDatabase.getIdByToken(receive.token).toString())

    }
}