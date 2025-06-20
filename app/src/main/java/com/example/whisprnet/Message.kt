package com.example.whisprnet

data class Message(
    val id: String,
    val content: String,
    val sender: String,
    val timestamp: Long,
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    SYSTEM,
    JOIN,
    LEAVE
}

data class ChatRoom(
    val code: String,
    val participants: MutableList<String> = mutableListOf(),
    val messages: MutableList<Message> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
)

data class WebSocketMessage(
    val type: String,
    val roomCode: String,
    val username: String? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
