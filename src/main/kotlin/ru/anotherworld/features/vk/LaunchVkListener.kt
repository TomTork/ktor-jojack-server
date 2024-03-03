package ru.anotherworld.features.vk

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import java.lang.Thread.sleep

fun configureVkLaunchListener() {
    Thread(Runnable {
        val vkGetPost = VkGetPost()
        while (true){
            vkGetPost.addToDatabase()
            sleep(360000) //delay on 6 min
        }
    }).start()
}