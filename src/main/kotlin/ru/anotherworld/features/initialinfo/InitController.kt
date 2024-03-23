package ru.anotherworld.features.initialinfo

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import ru.anotherworld.utils.InfoChatDatabase
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2

class InitController {
    suspend fun getInitialInfo(call: ApplicationCall){
        val tokenDatabase = TokenDatabase2()
        val mainDatabase = MainDatabase2()
        val infoChatDatabase = InfoChatDatabase()
        val receive = call.receive<Token2>()
        val login = receive.login
        val token = receive.token
        if(tokenDatabase.getTokenByLogin(login) == token){
            call.respond(InitRemote(tokenDatabase.getIdByLogin(login), mainDatabase.getJob(login),
                mainDatabase.getPrivacy(login), mainDatabase.getIcon(login),
                mainDatabase.getTrustLevel(login), mainDatabase.getInfo(login),
                infoChatDatabase.getAllByLogin(login)))
        }
        else call.respond(HttpStatusCode.BadRequest, InitRemote(-1, -1, false, "", -1,
            "", emptyList()
        ))
    }
}

@Serializable
private data class Token2(
    val login: String,
    val token: String
)
