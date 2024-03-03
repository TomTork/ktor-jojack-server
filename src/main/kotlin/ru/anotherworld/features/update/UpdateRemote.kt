package ru.anotherworld.features.update

import kotlinx.serialization.Serializable

@Serializable
data class Privacy(
    val token: String,
    val privacy: Boolean
)