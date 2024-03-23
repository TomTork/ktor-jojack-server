package ru.anotherworld.features.changepassword

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.anotherworld.Cipher
import ru.anotherworld.utils.MainDatabase2
import ru.anotherworld.utils.TokenDatabase2
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val limiter = ArrayList<Triple<String, Int, Long>>()


private fun getCurrentTimeStamp(time: Long): String? {
    val sdfDate = SimpleDateFormat("DD")
    val now = Date(time)
    return sdfDate.format(now)
}

private fun searchUserInLimiter(token: String): Boolean{
    println("DEBUG-INFO-> $limiter")
    for (element in limiter){
        if (token == element.first){
            if (element.second >= 3
                && getCurrentTimeStamp(element.third)!!.toLong()
                != getCurrentTimeStamp(System.currentTimeMillis())!!.toLong()){
                limiter.remove(element)
                limiter.add(Triple(token, 1, System.currentTimeMillis()))
                return true
            }
            else if(element.second < 3){
                val attempt = element.second
                limiter.remove(element)
                limiter.add(Triple(token, attempt + 1, System.currentTimeMillis()))
                return true
            }
            return false
        }
    }
    limiter.add(Triple(token, 1, System.currentTimeMillis()))
    return true
}


fun Application.configureChangePasswordRouting() {
    val mainDatabase2 = MainDatabase2()
    val tokenDatabase2 = TokenDatabase2()
    val cipher = Cipher()
    routing {
        post("/changepassword"){
            val receive = call.receive<ChangePassword>()
            if (searchUserInLimiter(receive.token)){
                val login = tokenDatabase2.getLoginByToken(receive.token)
                val currentPassword = mainDatabase2.getPassword(login)
                if (cipher.hash(receive.hashCurrentPassword) == currentPassword){ //passwords are equal
                    mainDatabase2.setPassword(login, cipher.hash(receive.hashNewPassword))
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.Conflict, "Passwords are not equal!")
            }
            else call.respond(HttpStatusCode.Conflict, "The number of attempts has been exceeded. Try again tomorrow.")

        }
    }
}

@Serializable
data class ChangePassword(
    val token: String,
    val hashCurrentPassword: String,
    val hashNewPassword: String
)