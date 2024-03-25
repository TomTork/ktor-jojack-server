package ru.anotherworld

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import ru.anotherworld.chats.di.mainModule
import ru.anotherworld.chats.two.configureChatTwoController
import ru.anotherworld.chats.zmultichat.configureWebSocketsMultiChat
import ru.anotherworld.features.changepassword.configureChangePasswordRouting
import ru.anotherworld.features.comments.configureCommentsRouting
import ru.anotherworld.features.createpost.configureCreatePostRouting
import ru.anotherworld.features.info.configureInfoRouting
import ru.anotherworld.features.initialinfo.configureInitRouting
import ru.anotherworld.features.insertchat.configureInsertChatRouting
import ru.anotherworld.features.login.configureLoginRouting
import ru.anotherworld.features.posts.configureLikeListener
import ru.anotherworld.features.register.configureRegisterRouting
import ru.anotherworld.features.search.configureSearchRouting
import ru.anotherworld.features.terminal.configureTerminalRouting
import ru.anotherworld.features.update.configureUpdateRouting
import ru.anotherworld.features.upload.icons.configureIconRouting
import ru.anotherworld.features.upload.images.configureImageRouting
import ru.anotherworld.features.vk.configureVkLaunchListener
import ru.anotherworld.features.vk.configureVkRouting
import ru.anotherworld.plugins.*
import ru.anotherworld.utils.*

const val globalPath = "C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files"
const val path = "C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld"
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(mainModule)
    }
    configureSockets()
    configureRouting()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureSecurity()
    configureInfoRouting()
    configureVkRouting()
    configureWebSocketsMultiChat()
    configureChatTwoController()
    configureLikeListener()
    configureSearchRouting()
    configureInitRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureUpdateRouting()
    configureImageRouting()
    configureIconRouting()
    configureCommentsRouting()
    configureInsertChatRouting()
    configureCreatePostRouting()
    configureTerminalRouting()
    configureChangePasswordRouting()
    configureVkLaunchListener()
    println("WARNING! ${InfoChatDatabase().getAllUsers()}")
}
