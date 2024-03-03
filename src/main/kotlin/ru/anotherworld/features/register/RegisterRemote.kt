package ru.anotherworld.features.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterReceiveRemote(
    val login: String,
    val password: String,
    val privateKey: String,
    val publicKey: String
)

@Serializable
data class RegisterResponceRemote(
    val token: String
)
