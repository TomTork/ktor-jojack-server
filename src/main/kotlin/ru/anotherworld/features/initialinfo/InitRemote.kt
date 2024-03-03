package ru.anotherworld.features.initialinfo

import kotlinx.serialization.Serializable

@Serializable
data class InitRemote(
    val id: Int,
    val job: Int,
    val privacy: Boolean,
    val icon: String,
    val trustLevel: Int,
    val info: String
)