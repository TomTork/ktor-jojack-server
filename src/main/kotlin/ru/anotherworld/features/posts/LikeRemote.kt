package ru.anotherworld.features.posts

import kotlinx.serialization.Serializable

@Serializable
data class RegisterLike(val url: String, val status: Boolean, val token: String)