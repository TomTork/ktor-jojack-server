package ru.anotherworld.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Runnable
import ru.anotherworld.chats.two.MessengerController3
import ru.anotherworld.utils.DatabaseSingletonVkPostDatabase

fun Application.configureDatabases() {
    Thread(Runnable {
        DatabaseSingletonVkPostDatabase.init()
    }).start()
    routing {
//        Thread(Runnable {
//            DatabaseSingletonMainDatabase.init()
//        }).start()

//        Thread(Runnable {
//            DatabaseSingletonLikesDatabase.init()
//        }).start()
//        Thread(Runnable {
//            DatabaseSingletonTokenDatabase.init()
//        }).start()
    }
}
