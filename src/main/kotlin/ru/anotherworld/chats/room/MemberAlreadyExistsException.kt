package ru.anotherworld.chats.room

class MemberAlreadyExistsException: Exception(
    "There is already a member with that username already in the room."
)