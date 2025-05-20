package com.example.venusai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.venusai.R
import com.example.venusai.data.BotFollower
import com.example.venusai.ui.components.ProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotListScreen(
    bots: List<BotFollower>,
    onBotClick: (BotFollower) -> Unit,
    onBack: () -> Unit,
    isPremiumUser: Boolean = false
) {
    // Separar bots premium y normales
    val botsPremium = bots.filter { it.premium }
    val botsRegulares = bots.filter { !it.premium }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona un bot para chatear") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Texto explicativo sobre los bots de IA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¿Con qué tipo de IA te gustaría chatear?",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(height = 8)
                    Text(
                        text = "Selecciona cualquiera de nuestros bots conversacionales para iniciar una conversación. Cada bot tiene su propia personalidad única y responderá según su estilo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(height = 8)
                    Text(
                        text = "Nota: Los bots también responderán automáticamente a tus publicaciones en la sección de Inicio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sección de bots premium
                if (botsPremium.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bots Premium",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isPremiumUser) "Acceso exclusivo a bots avanzados con capacidades superiores" 
                                else "Hazte premium para desbloquear estos bots exclusivos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    items(botsPremium) { bot ->
                        BotItem(
                            bot = bot,
                            onSelect = { onBotClick(bot) },
                            isPremium = true,
                            userIsPremium = isPremiumUser
                        )
                        Divider()
                    }
                    
                    item {
                        Spacer(height = 24)
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Spacer(height = 8)
                        Text(
                            text = "Bots Estándar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Bots regulares
                items(botsRegulares) { bot ->
                    BotItem(
                        bot = bot,
                        onSelect = { onBotClick(bot) },
                        isPremium = false,
                        userIsPremium = isPremiumUser
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun BotItem(
    bot: BotFollower,
    onSelect: () -> Unit,
    isPremium: Boolean = false,
    userIsPremium: Boolean = false
) {
    val cardBackgroundColor = if (isPremium) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val isLocked = isPremium && !userIsPremium
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = cardBackgroundColor,
        shape = if (isPremium) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                // Imagen de perfil del bot
                ProfileImage(
                    imageUrl = bot.fotoUrl,
                    size = 48,
                    modifier = if (isPremium) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            shape = CircleShape
                        )
                    } else Modifier
                )
                
                // Mostrar el icono de candado para bots premium bloqueados
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                // Mostrar estrella para bots premium desbloqueados
                if (isPremium && userIsPremium) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bot.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isPremium) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (bot.personalidad.isNotEmpty()) {
                    Text(
                        text = bot.personalidad,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (bot.descripcion.isNotEmpty()) {
                    Text(
                        text = bot.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                // Mensaje para bots premium bloqueados
                if (isLocked) {
                    Spacer(height = 8)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hazte premium para desbloquear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Spacer(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
} 