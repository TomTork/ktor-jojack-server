package ru.anotherworld.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import ru.anotherworld.chats.session.ChatSession
import ru.anotherworld.chats.two.NameDB
import ru.anotherworld.features.login.cipher
import ru.anotherworld.utils.TokenDatabase2

fun Application.configureSecurity() {
    install(Sessions){
        cookie<ChatSession>("SESSION")
        cookie<NameDB>("SESSION2")
    }
    intercept(ApplicationCallPipeline.Features) {
        if(call.sessions.get<ChatSession>() == null){
            val tokenDatabase = TokenDatabase2()
            val username = call.parameters["username"] ?: "Guest"
            val token = call.parameters["token"]
            println("STAGE-PRE=9 $username $token")
            if("username" !in call.parameters.names().toList() && "token" !in call.parameters.names().toList())call.sessions.set(ChatSession(username, generateNonce()))
            else if(username == "Guest" && token != null) call.sessions.set(ChatSession(tokenDatabase.getLoginByToken(token), generateNonce()))
            else if ("username" in call.parameters.names().toList() && token != "" && token != null && username == tokenDatabase.getLoginByToken(token))call.sessions.set(ChatSession(username, generateNonce()))
            else if(token != null) call.sessions.set(ChatSession(token, generateNonce()))
            else call.respond(HttpStatusCode.BadGateway, "Token not equal by login")
        }
        else if(call.sessions.get<NameDB>() == null){
            val nameDB = call.parameters["namedb"] ?: "name"
            val token = call.parameters["token"] ?: "token"
            call.sessions.set(NameDB(nameDB, token))
        }
    }

    authentication {
            oauth("auth-oauth-google") {
                urlProvider = { "http://localhost:8080/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = System.getenv("GOOGLE_CLIENT_ID"),
                        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                    )
                }
                client = HttpClient(Apache)
            }
        }
    authentication {
        basic(name = "myauth1") {
            realm = "Ktor Server"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        form(name = "myauth2") {
            userParamName = "user"
            passwordParamName = "password"
            challenge {
                /**/
            }
        }
    }

}
