package ru.anotherworld.features.initialinfo

import kotlinx.serialization.Serializable
import ru.anotherworld.utils.MyInfoChat

@Serializable
data class InitRemote(
    val id: Int,
    val job: Int,
    val privacy: Boolean,
    val icon: String,
    val trustLevel: Int,
    val info: String,
    val chatsList: List<MyInfoChat>
)