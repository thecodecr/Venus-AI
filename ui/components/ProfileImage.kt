package com.example.venusai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.example.venusai.R

@Composable
fun ProfileImage(
    imageUrl: String?,
    size: Int,
    modifier: Modifier = Modifier
) {
    val imageModifier = modifier
        .size(size.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    
    if (imageUrl.isNullOrEmpty()) {
        // Si no hay URL, mostrar un placeholder
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            modifier = imageModifier,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    } else {
        // Cargar imagen con Coil
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            contentDescription = "Foto de perfil",
            modifier = imageModifier
        )
    }
} 