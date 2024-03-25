package ru.anotherworld.features.insertchat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.anotherworld.utils.InfoChat
import ru.anotherworld.utils.InfoChatDatabase
import ru.anotherworld.utils.MyInfoChat
import ru.anotherworld.utils.TokenDatabase2

fun Application.configureInsertChatRouting(){
    val tokenDatabase2 = TokenDatabase2()
    val infoChatDatabase = InfoChatDatabase()
    routing {
        get("/getloginfromid"){
            val id = call.parameters["id"]
            if(id != null){
                try {
                    call.respond(HttpStatusCode.OK, tokenDatabase2.getLogin(id.toInt()))
                } catch (e: Exception){
                    call.respond(HttpStatusCode.Gone)
                }
            }
            else call.respond(HttpStatusCode.BadRequest)
        }
        post("/addnewchatinfo"){
            val receive = call.receive<InfoChatReceive>()
            println("STATUS-1 ${receive.login} ${receive.token} ${tokenDatabase2.getLoginByToken(receive.token)}")
            if (tokenDatabase2.getLoginByToken(receive.token) == receive.login){
                infoChatDatabase.insertAll(
                    InfoChat(
                        receive.login,
                        receive.urlChat,
                        receive.nameChat,
                        receive.users,
                        receive.iconChat
                    )
                )
                call.respond(HttpStatusCode.OK)
            }
            else call.respond(HttpStatusCode.BadRequest)
        }
        post("/addnewchatinfo2"){
            val receive = call.receive<InfoChat>()
            println("STATUS-2 ${receive.login}}")
            try{
                infoChatDatabase.insertAll(
                    InfoChat(
                        receive.login,
                        receive.urlChat,
                        receive.nameChat,
                        receive.users,
                        receive.iconChat
                    )
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception){
                call.respond(HttpStatusCode.Gone)
            }
        }
        post("/editchatinfo"){
            val receive = call.receive<InfoChatReceive>()
            if (tokenDatabase2.getLoginByToken(receive.token) == receive.login){
                infoChatDatabase.editData(
                    InfoChat(
                        receive.login,
                        receive.urlChat,
                        receive.nameChat,
                        receive.users,
                        receive.iconChat
                    )
                )
                call.respond(HttpStatusCode.OK)
            }
            else call.respond(HttpStatusCode.BadRequest)
        }
        post("/deletechatinfo"){
            val urlChat = call.parameters["url"]
            val token = call.parameters["token"]
            if(urlChat != null && token != null){
                infoChatDatabase.deleteChatByUrlChatAndLogin(urlChat, tokenDatabase2.getLoginByToken(token))
                call.respond(HttpStatusCode.OK)
            }
            else call.respond(HttpStatusCode.BadRequest)
        }
    }
}

@Serializable
data class InfoChatReceive(
    val token: String,
    val login: String,
    val urlChat: String,
    val nameChat: String,
    val users: String,
    val iconChat: String
)
