package ru.anotherworld.features.vk

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val iconUrl: String,
    val groupName: String,
    val textPost: String,
    val imagesUrls: VkImageAndVideo,
    val like: Int,
    val commentsUrl: String,
    val originalUrl: String,
    val exclusive: Boolean,
    val reposted: Boolean,
    val origName: String? = "",
    val origPost: String? = ""
)

@Serializable
data class GetPost(
    val startIndex: Int,
    val endIndex: Int,
    val token: String
)

@Serializable
data class GetRPost(
    val post: ArrayList<Post>
)