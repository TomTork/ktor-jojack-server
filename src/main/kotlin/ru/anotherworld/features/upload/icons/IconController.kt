package ru.anotherworld.features.upload.icons

import io.ktor.server.application.*
import io.ktor.server.request.*
import ru.anotherworld.globalPath
import java.io.File

class IconController {
    suspend fun getIcon(call: ApplicationCall, login: String) : File?{
        return File("$globalPath/images/icons/${login}.png") ?: null
    }
}