package ru.anotherworld.features.update

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2

class UpdateController {
    private val mainDatabase = MainDatabase2()
    private val tokenDatabase = TokenDatabase2()
    suspend fun setPrivacy(call: ApplicationCall){
        val data = call.receive<Privacy>()
        mainDatabase.setPrivacy(tokenDatabase.getLoginByToken(data.token), data.privacy)
    }
}