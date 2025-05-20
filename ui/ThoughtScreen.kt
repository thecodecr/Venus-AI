package com.example.venusai.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.venusai.R
import com.example.venusai.data.BotComment
import com.example.venusai.data.BotFollower
import com.example.venusai.data.CommentReply
import com.example.venusai.data.Thought
import com.example.venusai.data.ThoughtRepository
import com.example.venusai.data.UserProfile
import com.example.venusai.ui.theme.VenusPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.venusai.ui.components.ProfileImage
import com.example.venusai.ui.CommentReplyItem
import java.util.UUID
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThoughtScreen(
    userProfile: UserProfile,
    thoughts: List<Thought>,
    comments: Map<String, List<BotComment>>,
    expandedPostId: String? = null,
    onToggleExpand: (String) -> Unit = {},
    onPublish: (String, String?) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onBack: () -> Unit,
    onLike: (String, String) -> Unit = { _, _ -> },
    onRetweet: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var texto by remember { mutableStateOf("") }
    val maxChars = 280
    var editingThoughtId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isLoading by remember { mutableStateOf(false) }
    
    // Variable para la imagen seleccionada
    var selectedImageUriString by remember { mutableStateOf<String?>(null) }
    
    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUriString = uri?.toString()
    }
    
    // Mostrar la imagen seleccionada si existe
    val imagenUri = selectedImageUriString?.let { uri ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen seleccionada",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(180.dp),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            )
            
            IconButton(
                onClick = { selectedImageUriString = null },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Eliminar imagen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿Qué estás pensando?") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Área de publicación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileImage(
                            imageUrl = userProfile.fotoPerfilUri ?: "",
                            size = 48
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userProfile.nombre + " " + userProfile.apellidos,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@" + userProfile.usuario,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { if (it.length <= maxChars) texto = it },
                        label = { Text("¿Qué estás pensando?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Mostrar la imagen seleccionada si existe
                    imagenUri
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (texto.isNotBlank()) {
                                    isLoading = true
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    onPublish(texto, selectedImageUriString)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Publicando pensamiento...")
                                        // Simulación de tiempo de respuesta
                                        kotlinx.coroutines.delay(800)
                                        isLoading = false
                                        texto = ""
                                        selectedImageUriString = null
                                    }
                                }
                            },
                            enabled = texto.isNotBlank() && !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Publicar")
                            }
                        }
                    }
                }
            }
            
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lista de pensamientos
            if (thoughts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay pensamientos publicados aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(thoughts, key = { it.id }) { thought ->
                        ThoughtItem(
                            thought = thought,
                            userProfile = userProfile,
                            comments = comments[thought.id] ?: emptyList(),
                            isExpanded = expandedPostId == thought.id,
                            onToggleExpand = { 
                                onToggleExpand(thought.id)
                            },
                            onDelete = { 
                                onDelete(thought.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Pensamiento eliminado")
                                }
                            },
                            onEdit = { thoughtId, newText ->
                                onEdit(thoughtId, newText)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Pensamiento actualizado")
                                }
                            },
                            onStartEdit = { thoughtId, currentText ->
                                editingThoughtId = thoughtId
                                editingText = currentText
                            },
                            isEditing = editingThoughtId == thought.id,
                            editingText = if (editingThoughtId == thought.id) editingText else thought.texto,
                            onEditTextChange = { 
                                if (it.length <= maxChars) editingText = it 
                            },
                            onCancelEdit = {
                                editingThoughtId = null
                                editingText = ""
                            },
                            onLikeClick = { thoughtId ->
                                onLike(thoughtId, userProfile.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Has dado like")
                                }
                            },
                            onRetweetClick = { thoughtId ->
                                onRetweet(thoughtId, userProfile.id, thought.texto)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Has retuiteado")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThoughtItem(
    thought: Thought,
    userProfile: UserProfile,
    comments: List<BotComment>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (String, String) -> Unit,
    onStartEdit: (String, String) -> Unit,
    isEditing: Boolean,
    editingText: String,
    onEditTextChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onLikeClick: (String) -> Unit,
    onRetweetClick: (String) -> Unit
) {
    val maxChars = 280
    val canEdit = System.currentTimeMillis() - thought.timestamp < 5 * 60 * 1000 // 5 minutos
    val dateFormat = SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault())
    val context = LocalContext.current
    
    // Estado para saber si el usuario actual dio like
    val userLiked = thought.likes.contains(userProfile.id)
    // Estado para saber si el usuario retuiteó
    val userRetweeted = thought.retweets.contains(userProfile.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado: foto de perfil, nombre, usuario, hora
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileImage(
                    imageUrl = userProfile.fotoPerfilUri ?: "",
                    size = 48
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile.nombre + " " + userProfile.apellidos,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@" + userProfile.usuario,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = " · " + dateFormat.format(Date(thought.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        // Mostrar si es un retweet
                        if (thought.isRetweet) {
                            Text(
                                text = "Retuiteado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contenido del pensamiento o campo de edición
            if (isEditing) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditTextChange,
                    label = { Text("Editar pensamiento") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${editingText.length}/$maxChars")
                    
                    Row {
                        Button(
                            onClick = { onEdit(thought.id, editingText) },
                            enabled = editingText.isNotBlank()
                        ) { 
                            Text("Guardar") 
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onCancelEdit
                        ) { 
                            Text("Cancelar") 
                        }
                    }
                }
            } else {
                Text(
                    text = thought.texto,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // Mostrar la imagen si existe
                if (thought.imageUri != null && thought.imageUri.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thought.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen del pensamiento",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(42.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contador de interacciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (thought.likes.isNotEmpty()) {
                        Text(
                            text = "${thought.likes.size} Me gusta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (thought.retweets.isNotEmpty()) {
                        Text(
                            text = "${thought.retweets.size} Retweets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Botones de interacción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        IconButton(
                            onClick = { onLikeClick(thought.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite, 
                                contentDescription = "Me gusta",
                                tint = if (userLiked) Color.Red else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Ocultar comentarios" else "Mostrar comentarios",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                                // Mostrar número de comentarios
                                val thoughtComments = comments
                                val commentCount = thoughtComments.size
                                if (commentCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    androidx.compose.animation.animateColorAsState(
                                        targetValue = if (isExpanded) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        else 
                                            MaterialTheme.colorScheme.primary,
                                        label = "CommentCountColor"
                                    ).value.let { color ->
                                        Text(
                                            text = "$commentCount",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = color,
                                            fontWeight = if (isExpanded) FontWeight.Normal else FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        IconButton(
                            onClick = { onRetweetClick(thought.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Share, 
                                contentDescription = "Retuitear",
                                tint = if (userRetweeted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Row {
                        if (canEdit) {
                            TextButton(
                                onClick = { onStartEdit(thought.id, thought.texto) }
                            ) { 
                                Text("Editar", style = MaterialTheme.typography.bodySmall) 
                            }
                        }
                        
                        TextButton(
                            onClick = onDelete
                        ) { 
                            Text(
                                "Eliminar", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            ) 
                        }
                    }
                }
            }
            
            // Comentarios
            AnimatedVisibility(visible = isExpanded && comments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        "Comentarios (${comments.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    BotCommentList(comments, userProfile)
                }
            }
        }
    }
}

@Composable
fun BotCommentList(comments: List<BotComment>, userProfile: UserProfile) {
    LazyColumn {
        items(comments) { comment ->
            BotCommentItem(comment = comment, userProfile = userProfile)
            Divider(thickness = 0.5.dp)
        }
    }
}

@Composable
fun BotCommentItem(comment: BotComment, userProfile: UserProfile) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userLiked = comment.likes.contains(userProfile.id)
    var showReplyDialog by remember { mutableStateOf(false) }
    var showReplies by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Información del bot
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del bot - Cambiamos a usar ProfileImage
            ProfileImage(
                imageUrl = comment.bot.fotoUrl,
                size = 40
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Nombre y descripción del bot
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.bot.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (comment.bot.premium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(VenusPrimary, CircleShape)
                                .border(1.dp, Color.White, CircleShape),
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
                
                if (comment.bot.personalidad.isNotEmpty()) {
                    Text(
                        text = comment.bot.personalidad,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Texto del comentario
        Text(
            text = comment.texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 52.dp)
        )
        
        // Botones de interacción
        Row(
            modifier = Modifier
                .padding(start = 52.dp, top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de respuesta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showReplyDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Reply,
                    contentDescription = "Responder",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                if (comment.replies.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${comment.replies.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Si hay respuestas, botón para verlas
            if (comment.replies.isNotEmpty()) {
                TextButton(
                    onClick = { showReplies = !showReplies },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (showReplies) "Ocultar respuestas" else "Ver respuestas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Sección de respuestas (visible solo si showReplies es true)
        if (showReplies && comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 52.dp, top = 8.dp)
                    .fillMaxWidth()
            ) {
                comment.replies.forEach { reply ->
                    CommentReplyItem(reply)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
        
        // Diálogo para añadir respuesta
        if (showReplyDialog) {
            var replyText by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showReplyDialog = false },
                title = { Text("Responder a ${comment.bot.nombre}") },
                text = {
                    Column {
                        Text(
                            text = "Comentario original: ${comment.texto}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Escribe tu respuesta...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (replyText.trim().isNotEmpty()) {
                                scope.launch {
                                    val reply = CommentReply(
                                        id = UUID.randomUUID().toString(),
                                        userId = userProfile.id,
                                        userName = "${userProfile.nombre} ${userProfile.apellidos}",
                                        userImageUri = userProfile.fotoPerfilUri,
                                        texto = replyText.trim(),
                                        timestamp = System.currentTimeMillis()
                                    )
                                    ThoughtRepository.addReplyToComment(
                                        context,
                                        comment.id.split("-")[0],
                                        comment.id,
                                        reply
                                    )
                                    showReplyDialog = false
                                    showReplies = true // Mostrar respuestas automáticamente
                                }
                            }
                        },
                        enabled = replyText.trim().isNotEmpty()
                    ) {
                        Text("Responder")
                    }
                },
                dismissButton = {
                    Button(onClick = { showReplyDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}