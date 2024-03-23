package ru.anotherworld.features.register

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.anotherworld.Cipher
import ru.anotherworld.utils.*
import java.sql.SQLException
import java.util.*

val cipher = Cipher()
class RegisterController(){
    private val mainDatabase = MainDatabase2()
    private val tokenDatabase2 = TokenDatabase2()
    suspend fun registerNewUser(call: ApplicationCall){
        val registerReceiverRemote = call.receive<RegisterReceiveRemote>()
        val isUserExists = mainDatabase.searchRegisterUser(registerReceiverRemote.login)

        if(isUserExists){
            call.respond(HttpStatusCode.Conflict, "User already exists")
        }
        else{
            val token = UUID.randomUUID().toString()
            mainDatabase.insertAll(
                JoJack(registerReceiverRemote.login, cipher.hash(registerReceiverRemote.password),
                0, 0, false, "",
                    registerReceiverRemote.privateKey, registerReceiverRemote.publicKey, ""))
            mainDatabase.setOpenedKey(registerReceiverRemote.login, registerReceiverRemote.publicKey)
            mainDatabase.setClosedKey(registerReceiverRemote.login, registerReceiverRemote.privateKey)
            try {
                tokenDatabase2.insertAll(TokensA2(registerReceiverRemote.login, token))
            } catch (e: SQLException){
                call.respond(HttpStatusCode.Conflict, "User already exists")
            }
            call.respond(RegisterResponceRemote(token))
        }
    }
}