package ru.anotherworld.features.upload.images

import ru.anotherworld.globalPath
import java.io.File

class ImageController {
    suspend fun getImage(name: String): File? {
        return try{ File("$globalPath/images/others/$name.png") ?: null } catch (e: Exception) { null }
    }
}