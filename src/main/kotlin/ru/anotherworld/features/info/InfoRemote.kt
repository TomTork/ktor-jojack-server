package ru.anotherworld.features.info

import kotlinx.serialization.Serializable

@Serializable
data class InfoRemote(
    val maxId: Int
)