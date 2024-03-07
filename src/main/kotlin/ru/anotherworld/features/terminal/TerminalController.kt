package ru.anotherworld.features.terminal

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.anotherworld.features.login.cipher
import ru.anotherworld.path
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.io.File

class TerminalController {
    private val tokenDatabase = TokenDatabase2()
    private val mainDatabase = MainDatabase2()
    private val key1 = File("$path/keys",
        "first_key.txt").readText()
    private val key2 = File("$path/keys",
        "second_key.txt").readText()
    suspend fun processingCommand(call: ApplicationCall){
        val receive = call.receive<CommandLine>()
        if (mainDatabase.getJob(tokenDatabase.getLoginByToken(receive.token)) >= 4){
            val answer = Answer("Success!")
            call.respond<Answer>(answer)
        }
        else if(receive.password != null) {
            try {
                if(cipher.hash(cipher.decrypt(receive.password, key1)) == "58a7273cae5316586bbd412d8d15f7b74b8885974400b9a84453d1a4c497831d39d16552370a18f36454eb44c78e1b6eaacd84fd51b240872ceeba85311e4f5e"){
                    val answer = Answer("Success!")
                    call.respond<Answer>(answer)
                }
                else {
                    val answer = Answer("Invalid query or no access.")
                    call.respond<Answer>(answer)
                }
            } catch (e: Exception){
                println(e)
            }
            try {
                if(cipher.hash(cipher.decrypt(receive.password, key2)) == "8b53086ae560a67b0263f2ce6e9321b8afd28e01ce9001096f44a2ac6a4a8f0dea4d943783b7c307d2e95df9bf4fbc617f953abfd6446f242b710ce6d5a2648e"){
                    val answer = Answer("Success!")
                    call.respond<Answer>(answer)
                }
                else {
                    val answer = Answer("Invalid query or no access.")
                    call.respond<Answer>(answer)
                }
            } catch (e: Exception){
                println(e)
            }
        }
        else {
            val answer = Answer("Invalid query or no access.")
            call.respond<Answer>(answer)
        }
    }
}

@Serializable
data class CommandLine(
    val query: String,
    val token: String,
    val password: String? = ""
)

@Serializable
data class Answer(
    val answer: String
)