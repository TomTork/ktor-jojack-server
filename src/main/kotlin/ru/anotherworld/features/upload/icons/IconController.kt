package ru.anotherworld.features.upload.icons

import io.ktor.server.application.*
import io.ktor.server.request.*
import java.io.File

class IconController {
    suspend fun getIcon(call: ApplicationCall, login: String) : File?{
        return File("C:/Users/Rescue/Documents/ktor-jojack-server/src/main/kotlin/ru/anotherworld/files/images/icons/${login}.png") ?: null
    }
}