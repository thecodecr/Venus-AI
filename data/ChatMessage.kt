package com.example.venusai.data

data class ChatMessage(
    val id: String,
    val text: String,
    val isUserMessage: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) 