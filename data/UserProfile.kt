package com.example.venusai.data

data class UserProfile(
    val id: String = "user_id",
    val nombre: String = "",
    val apellidos: String = "",
    val usuario: String = "",
    val fotoPerfilUri: String? = null,
    val biografia: String = "",
    val intereses: String = "",
    val ubicacion: String = "",
    val isPremium: Boolean = false
) 