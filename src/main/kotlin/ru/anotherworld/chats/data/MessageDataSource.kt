package ru.anotherworld.chats.data

import ru.anotherworld.chats.data.model.Message

interface MessageDataSource {
    suspend fun getAllMessages(): List<Message>

    suspend fun insertMessage(message: Message)
}