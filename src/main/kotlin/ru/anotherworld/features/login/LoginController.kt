package ru.anotherworld.features.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.anotherworld.Cipher
import ru.anotherworld.features.register.cipher
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2

val cipher = Cipher()
class LoginController {
    suspend fun performLogin(call: ApplicationCall){
        val receive = call.receive<LoginReceiveRemote>()
        val mainDatabase = MainDatabase2()
        val isUserExists = mainDatabase.searchRegisterUser(receive.login)
        if(!isUserExists){
            call.respond(HttpStatusCode.BadRequest,
                LoginResponseRemote("User not found", "", ""))
        }
        else{
            if (receive.login != "" && receive.password != ""){
                if (mainDatabase.equalPassword(receive.login, cipher.hash(receive.password))){
                    val tokenDatabase = TokenDatabase2()
//                    val token = UUID.randomUUID().toString()
//                    mainDatabase.updateAll(receive.login, receive.password,
//                        0, 0, cipher.generatePassword(), cipher.generatePassword(), "")
//                    tokenDatabase.updateAll(tokenDatabase.getIdByLogin(receive.login), receive.login, token)
                    call.respond(LoginResponseRemote(tokenDatabase.getTokenByLogin(receive.login),
                        mainDatabase.getClosedKey(receive.login), mainDatabase.getOpenedKey(receive.login)))
                }
                else{
                    call.respond(HttpStatusCode.BadRequest,
                        LoginResponseRemote("Invalid password or login", "", ""))
                }
            }
            else{
                call.respond(HttpStatusCode.BadRequest,
                    LoginResponseRemote("Fields are empty", "", ""))
            }
        }
    }
}