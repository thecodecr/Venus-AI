package com.example.venusai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.venusai.data.CommentReply
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente reutilizable para mostrar una respuesta a un comentario
 */
@Composable
fun CommentReplyItem(reply: CommentReply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Cabecera con información del usuario
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Image(
                painter = rememberAsyncImagePainter(reply.userImageUri ?: ""),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Nombre de usuario
            Text(
                text = reply.userName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Fecha de la respuesta
            val dateFormatter = remember { SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()) }
            Text(
                text = dateFormatter.format(Date(reply.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        // Texto de la respuesta
        Text(
            text = reply.texto,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 32.dp, top = 4.dp)
        )
    }
} 