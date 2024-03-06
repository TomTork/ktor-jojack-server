package ru.anotherworld

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import ru.anotherworld.chats.di.mainModule
import ru.anotherworld.chats.two.configureChatTwoController
import ru.anotherworld.features.info.configureInfoRouting
import ru.anotherworld.features.initialinfo.configureInitRouting
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


fun main() {
    embeddedServer(Netty, port = 8080, host = "192.168.0.100", module = Application::module)
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
    configureChatTwoController()
    configureLikeListener()
    configureSearchRouting()
    configureInitRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureUpdateRouting()
    configureImageRouting()
    configureIconRouting()
    configureTerminalRouting()
    configureVkLaunchListener()
}
