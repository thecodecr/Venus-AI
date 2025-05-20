package com.example.venusai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.venusai.data.BotFollower
import com.example.venusai.data.UserProfile
import com.example.venusai.ui.theme.AppIcons
import com.example.venusai.ui.theme.VenusPrimary
import com.example.venusai.ui.components.ProfileImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    followers: List<BotFollower>,
    following: List<BotFollower>,
    isPremiumUser: Boolean = false,
    onFollowToggle: (BotFollower) -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    onEditProfile: () -> Unit = {},
    onStartChat: (BotFollower?) -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Seguidores", "Siguiendo")
    var selectedBot by remember { mutableStateOf<BotFollower?>(null) }
    var showBotDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    // Botón para alternar entre modo claro y oscuro
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) AppIcons.LightMode else AppIcons.DarkMode,
                            contentDescription = if (isDarkTheme) "Cambiar a modo claro" else "Cambiar a modo oscuro"
                        )
                    }
                    
                    // Botón de configuración
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            AppIcons.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarjeta de perfil
            ProfileCard(
                userProfile = userProfile,
                isPremiumUser = isPremiumUser,
                followersCount = followers.size,
                followingCount = following.size,
                onDeleteAccount = onDeleteAccount
            )
            
            // Pestañas para seguidores y seguidos
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Lista de seguidores o seguidos según la pestaña seleccionada
            when (tabIndex) {
                0 -> FollowersList(
                    bots = followers,
                    isPremiumUser = isPremiumUser,
                    onFollowToggle = onFollowToggle,
                    onBotSelected = { 
                        selectedBot = it
                        showBotDialog = true
                    }
                )
                1 -> FollowersList(
                    bots = following,
                    isPremiumUser = isPremiumUser,
                    onFollowToggle = onFollowToggle,
                    onBotSelected = { 
                        selectedBot = it
                        showBotDialog = true
                    }
                )
            }
            
            // Diálogo para seleccionar un bot para chatear
            if (showBotDialog && selectedBot != null) {
                AlertDialog(
                    onDismissRequest = { showBotDialog = false },
                    title = { Text("Chatear con ${selectedBot?.nombre}") },
                    text = {
                        Column {
                            Text("¿Quieres iniciar una conversación con este bot de IA?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Personalidad: ${selectedBot?.personalidad}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { 
                            onStartChat(selectedBot)
                            showBotDialog = false
                        }) {
                            Text("Iniciar chat")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBotDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Botón flotante para chat
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = { 
                        if (following.isNotEmpty()) {
                            selectedBot = following.first()
                            showBotDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chatear",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    userProfile: UserProfile,
    isPremiumUser: Boolean,
    followersCount: Int,
    followingCount: Int,
    onDeleteAccount: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                ProfileImage(
                    imageUrl = userProfile.fotoPerfilUri ?: "",
                    size = 80
                )
                
                if (isPremiumUser) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(VenusPrimary, CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "P", 
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = userProfile.nombre + " " + userProfile.apellidos,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "@" + userProfile.usuario,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            if (userProfile.biografia.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile.biografia,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (userProfile.intereses.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Intereses: " + userProfile.intereses,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (userProfile.ubicacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        AppIcons.LocationOn,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userProfile.ubicacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = followersCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seguidores",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = followingCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Siguiendo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (isPremiumUser) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Usuario Premium",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun FollowersList(
    bots: List<BotFollower>,
    isPremiumUser: Boolean,
    onFollowToggle: (BotFollower) -> Unit,
    onBotSelected: (BotFollower) -> Unit
) {
    LazyColumn {
        items(bots) { bot ->
            BotListItem(
                bot = bot,
                isPremiumUser = isPremiumUser,
                onFollowToggle = { onFollowToggle(bot) },
                onSelect = { onBotSelected(bot) }
            )
            Divider()
        }
    }
}

@Composable
fun BotListItem(
    bot: BotFollower,
    isPremiumUser: Boolean,
    onFollowToggle: () -> Unit,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            ProfileImage(
                imageUrl = bot.fotoUrl,
                size = 48
            )
            
            if (bot.premium && !isPremiumUser) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(VenusPrimary, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "P", 
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.8),
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bot.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = bot.personalidad,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        OutlinedButton(
            onClick = onFollowToggle,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("Seguir")
        }
    }
} 