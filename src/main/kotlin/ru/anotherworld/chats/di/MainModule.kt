package ru.anotherworld.chats.di

import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import ru.anotherworld.chats.data.MessageDataSource
import ru.anotherworld.chats.data.MessageDataSourceImpl
import ru.anotherworld.chats.room.RoomController

val mainModule = module {
    single {
        KMongo.createClient().coroutine.getDatabase("message_db")
    }
    single<MessageDataSource> {
        MessageDataSourceImpl(get())
    }
    single {
        RoomController(get())
    }
}