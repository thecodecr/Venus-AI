package com.example.venusai.data

data class Thought(
    val id: String,
    val texto: String,
    val timestamp: Long,
    val imageUri: String? = null,
    val likes: MutableList<String> = mutableListOf(), // IDs de los usuarios/bots que dieron like
    val retweets: MutableList<String> = mutableListOf(), // IDs de los usuarios/bots que retuitearon
    val isRetweet: Boolean = false, // Indica si es un retweet
    val originalThoughtId: String? = null // ID del pensamiento original si es un retweet
)

data class BotComment(
    val id: String,
    val bot: BotFollower,
    val texto: String,
    val likes: MutableList<String> = mutableListOf(), // IDs de los usuarios que dieron like
    val replies: MutableList<CommentReply> = mutableListOf() // Respuestas al comentario
)

// Eliminamos la clase CommentReply de aquí, ahora se encuentra en CommentModels.kt 