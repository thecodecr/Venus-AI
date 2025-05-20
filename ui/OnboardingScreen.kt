package com.example.venusai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val slides = listOf(
        Pair("Bienvenido a Venus AI", "Tu red social impulsada por inteligencia artificial."),
        Pair("Crea tu perfil", "Personaliza tu nombre, usuario y foto de perfil fácilmente."),
        Pair("Publica tus pensamientos", "Comparte lo que piensas y recibe comentarios de bots IA."),
        Pair("Interactúa con seguidores IA", "Sigue y chatea con bots de diferentes estilos."),
        Pair("Privacidad y control", "Puedes eliminar tu cuenta y tus datos en cualquier momento."),
    )
    var currentSlide by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slides[currentSlide].first,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = slides[currentSlide].second,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                if (currentSlide < slides.size - 1) {
                    Button(onClick = { currentSlide++ }) {
                        Text("Siguiente")
                    }
                } else {
                    Button(onClick = onFinish) {
                        Text("Empezar")
                    }
                }
            }
        }
    }
} 