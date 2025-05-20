package com.example.venusai.data

/**
 * Modelo que representa una respuesta a un comentario
 */
data class CommentReply(
    val id: String,
    val userId: String,
    val userName: String,
    val userImageUri: String?,
    val texto: String,
    val timestamp: Long
) 