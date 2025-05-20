package com.example.venusai.data

data class BotFollower(
    val id: String,
    val nombre: String,
    val fotoUrl: String,
    var seguido: Boolean = false,
    val personalidad: String = "",
    val descripcion: String = "",
    val estilo: String = "",
    val premium: Boolean = false
) 