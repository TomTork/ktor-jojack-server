package ru.anotherworld.chats.data

import org.litote.kmongo.coroutine.CoroutineDatabase
import ru.anotherworld.chats.data.model.Message

class MessageDataSourceImpl(
    private val db: CoroutineDatabase
) : MessageDataSource{

    private val messages = db.getCollection<Message>()

    override suspend fun getAllMessages(): List<Message> {
        return messages.find().descendingSort(Message::timestamp).toList()
    }

    override suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }
}