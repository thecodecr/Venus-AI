package com.example.venusai.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.venusai.R
import com.example.venusai.ai.OpenRouterApi
import com.example.venusai.ai.OpenRouterMessage
import com.example.venusai.ai.OpenRouterRequest
import com.example.venusai.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    thoughts: List<Thought>,
    comments: Map<String, List<BotComment>>,
    userProfile: UserProfile,
    selectedBots: List<BotFollower>,
    openRouterApi: OpenRouterApi,
    onAddThought: (String, Uri?) -> Unit,
    onLikeThought: (String) -> Unit,
    onRetweetThought: (String) -> Unit,
    onBack: () -> Unit,
    scope: CoroutineScope,
    model: String,
    apiKey: String
) {
    var showCreatePost by remember { mutableStateOf(false) }
    var editingThoughtId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    if (showCreatePost) {
        CreatePostScreen(
            userProfile = userProfile,
            onPublish = { text, uri ->
                onAddThought(text, uri)
                showCreatePost = false
            },
            onCancel = {
                showCreatePost = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreatePost = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Crear publicación")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(thoughts.sortedByDescending { it.timestamp }) { thought ->
                        ThoughtItem(
                            thought = thought,
                            comments = comments[thought.id] ?: emptyList(),
                            userProfile = userProfile,
                            onLike = { onLikeThought(thought.id) },
                            onRetweet = { onRetweetThought(thought.id) },
                            onEdit = { newText ->
                                scope.launch {
                                    ThoughtRepository.editThought(context, thought.id, newText)
                                    snackbarHostState.showSnackbar("Tweet editado correctamente")
                                    editingThoughtId = null
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    ThoughtRepository.deleteThought(context, thought.id)
                                    snackbarHostState.showSnackbar("Tweet eliminado correctamente")
                                }
                            },
                            canEdit = System.currentTimeMillis() - thought.timestamp < 5 * 60 * 1000, // 5 minutos
                            isEditing = editingThoughtId == thought.id,
                            onStartEdit = {
                                editingThoughtId = thought.id
                                editingText = thought.texto
                            },
                            editingText = if (editingThoughtId == thought.id) editingText else thought.texto,
                            onEditTextChange = { editingText = it },
                            onCancelEdit = { editingThoughtId = null }
                        )
                    }
                }
                
                FloatingActionButton(
                    onClick = { showCreatePost = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Crear publicación")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    userProfile: UserProfile,
    onPublish: (String, Uri?) -> Unit,
    onCancel: () -> Unit
) {
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear publicación") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onPublish(postText, selectedImageUri) },
                        enabled = postText.isNotBlank()
                    ) {
                        Text("Publicar", color = if (postText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Imagen de perfil
                Image(
                    painter = if (userProfile.fotoPerfilUri != null && userProfile.fotoPerfilUri.isNotEmpty()) {
                        rememberAsyncImagePainter(model = userProfile.fotoPerfilUri)
                    } else {
                        painterResource(id = R.drawable.default_profile)
                    },
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Campo de texto para el tweet
                TextField(
                    value = postText,
                    onValueChange = { postText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text("¿Qué está pasando?") },
                    textStyle = TextStyle(fontSize = 18.sp)
                )
            }
            
            // Mostrar imagen seleccionada
            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar imagen",
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Adjuntar imagen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Aquí podrían ir más opciones como encuestas, GIFs, emojis, etc.
            }
        }
    }
}

@Composable
fun ThoughtItem(
    thought: Thought,
    comments: List<BotComment>,
    userProfile: UserProfile,
    onLike: () -> Unit,
    onRetweet: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    editingText: String,
    onEditTextChange: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    val context = LocalContext.current
    val maxChars = 280
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var expandComments by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera: usuario y timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Image(
                        painter = if (userProfile.fotoPerfilUri != null && userProfile.fotoPerfilUri.isNotEmpty()) {
                            rememberAsyncImagePainter(model = userProfile.fotoPerfilUri)
                        } else {
                            painterResource(id = R.drawable.default_profile)
                        },
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "${userProfile.nombre} ${userProfile.apellidos}",
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = formatTimestamp(thought.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Mostrar opciones de edición solo si es un tweet del usuario
                Row {
                    if (canEdit) {
                        IconButton(
                            onClick = { onStartEdit() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar tweet",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar tweet",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contenido del tweet (editable o no)
            if (isEditing) {
                // Campo de edición
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { if (it.length <= maxChars) onEditTextChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Controles de edición
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${editingText.length}/$maxChars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .align(Alignment.CenterVertically)
                    )
                    
                    TextButton(
                        onClick = onCancelEdit
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = { onEdit(editingText) },
                        enabled = editingText.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            } else {
                // Contenido normal
                Text(
                    text = thought.texto,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Imagen adjunta
            thought.imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Imagen adjunta",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Acciones (me gusta, comentar, retuitear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Comentarios
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expandComments = !expandComments }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comentar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comments.size.toString(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Retweets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onRetweet() }
                ) {
                    Icon(
                        imageVector = if (thought.retweets.contains(userProfile.id)) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                        contentDescription = "Retuitear",
                        tint = if (thought.retweets.contains(userProfile.id)) Color(0xFF17BF63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = thought.retweets.size.toString(),
                        color = if (thought.retweets.contains(userProfile.id)) Color(0xFF17BF63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Me gusta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (thought.likes.contains(userProfile.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (thought.likes.contains(userProfile.id)) Color(0xFFE0245E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = thought.likes.size.toString(),
                        color = if (thought.likes.contains(userProfile.id)) Color(0xFFE0245E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Comentarios de bots (mejorados visualmente)
            if (comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                // Título de la sección de comentarios con opción para expandir
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandComments = !expandComments },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comentarios (${comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Icon(
                        imageVector = if (expandComments) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandComments) "Ocultar comentarios" else "Ver comentarios"
                    )
                }
                
                AnimatedVisibility(visible = expandComments) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        comments.forEach { comment ->
                            BotCommentItemImproved(comment = comment)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Si no está expandido, mostrar solo una vista previa
                if (!expandComments && comments.isNotEmpty()) {
                    BotCommentItemImproved(comment = comments.first())
                    
                    if (comments.size > 1) {
                        TextButton(
                            onClick = { expandComments = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Ver todos los ${comments.size} comentarios")
                        }
                    }
                }
            }
            
            // Diálogo de confirmación para eliminar
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Eliminar tweet") },
                    text = { Text("¿Estás seguro de que quieres eliminar este tweet?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDelete()
                                showDeleteConfirmation = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteConfirmation = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BotCommentItemImproved(comment: BotComment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar del bot
            Image(
                painter = rememberAsyncImagePainter(model = comment.bot.fotoUrl),
                contentDescription = "Avatar del bot",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                // Nombre del bot
                Text(
                    text = comment.bot.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Comentario
                Text(
                    text = comment.texto,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Mostrar likes si hay
                if (comment.likes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color(0xFFE0245E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = comment.likes.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// Utilidad para formatear timestamps
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "hace unos segundos"
        diff < 3_600_000 -> "${diff / 60_000} min"
        diff < 86_400_000 -> "${diff / 3_600_000} h"
        diff < 604_800_000 -> "${diff / 86_400_000} d"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

// Función para generar un prompt para comentarios de IA basado en la personalidad del bot
fun generateCommentPrompt(botName: String, botPersonality: String, postContent: String): String {
    return """
        Eres ${botName}, un bot con la siguiente personalidad: ${botPersonality}.
        
        Responde con un breve comentario natural y conversacional a la siguiente publicación:
        "${postContent}"
        
        Tu comentario debe reflejar tu personalidad y no debe superar los 100 caracteres.
        No uses hashtags ni emojis excesivos.
        Responde como si estuvieras en una red social, de manera casual y genuina.
    """.trimIndent()
} 