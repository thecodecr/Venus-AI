package com.example.venusai.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.venusai.data.ChatMessage
import com.example.venusai.data.BotFollower
import com.example.venusai.data.UserProfile
import com.example.venusai.ui.components.ProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    bot: BotFollower,
    messages: List<ChatMessage>,
    onSend: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var isSpeaking by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showBotSelector by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Usuario actual (debe pasarse desde la pantalla principal)
    val userProfile = UserProfile() // Default empty profile
    
    // Configuración de TextToSpeech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }
    }
    
    // Limpiar TextToSpeech al salir
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }
    
    // Permisos para grabación de voz
    val hasRecordPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Manejar el resultado del permiso
    }
    
    // Función para leer el texto
    fun speakText(text: String) {
        tts?.let { textToSpeech ->
            if (!isSpeaking) {
                isSpeaking = true
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }
                })
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MessageId")
            } else {
                textToSpeech.stop()
                isSpeaking = false
            }
        }
    }
    
    // Desplazarse al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mostrar un diálogo al iniciar para seleccionar con qué IA chatear
    LaunchedEffect(Unit) {
        showBotSelector = messages.isEmpty()
    }
    
    // Manejar el botón de retroceso
    BackHandler {
        focusManager.clearFocus()
        onBack()
    }
    
    if (showBotSelector) {
        AlertDialog(
            onDismissRequest = { showBotSelector = false },
            title = { Text("Chatear con ${bot.nombre}") },
            text = {
                Column {
                    Text("¿Sobre qué te gustaría hablar con este bot de IA?")
                    Text(
                        "Personalidad: ${bot.personalidad}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (bot.descripcion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            bot.descripcion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    showBotSelector = false
                    // Enfocar el campo de texto al cerrar el diálogo
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }) {
                    Text("Comenzar chat")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileImage(
                                imageUrl = bot.fotoUrl,
                                size = 40
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = bot.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = bot.personalidad,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            focusManager.clearFocus()
                            onBack() 
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp, // Aumentado para mayor visibilidad
                    shadowElevation = 8.dp // Añadido para dar sombra
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Campo de texto
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .focusRequester(focusRequester),
                            placeholder = { Text("Escribe un mensaje...") },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = false,
                            maxLines = 4
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Botón de enviar
                        FloatingActionButton(
                            onClick = {
                                if (input.isNotBlank()) {
                                    onSend(input)
                                    input = ""
                                    // Desplazarse al último mensaje después de enviar
                                    scope.launch {
                                        delay(100) // Pequeño retraso para asegurar que el mensaje se agrega
                                        if (messages.isNotEmpty()) {
                                            scrollState.animateScrollToItem(messages.size - 1)
                                        }
                                    }
                                }
                            },
                            containerColor = if (input.isNotBlank()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    // Mensaje de bienvenida cuando no hay mensajes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ProfileImage(
                            imageUrl = bot.fotoUrl,
                            size = 100
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "¡Hola! Soy ${bot.nombre}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = bot.personalidad,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (bot.descripcion.isNotEmpty()) {
                            Text(
                                text = bot.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Empieza a chatear conmigo enviando un mensaje",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Lista de mensajes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        state = scrollState,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(messages) { message ->
                            MessageItem(
                                message = message,
                                bot = bot,
                                userProfile = userProfile,
                                onSpeakText = { speakText(it) },
                                isSpeaking = isSpeaking
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    bot: BotFollower,
    userProfile: UserProfile,
    onSpeakText: (String) -> Unit,
    isSpeaking: Boolean
) {
    val isUserMessage = message.isUserMessage
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start
    val bubbleColor = if (isUserMessage) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUserMessage) 
        Color.White 
    else 
        MaterialTheme.colorScheme.onSecondaryContainer
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Nombre del remitente (sólo para mensajes del bot)
        if (!isUserMessage) {
            Text(
                text = bot.nombre,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }
        
        // Burbuja de mensaje
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Avatar (solo para mensajes del bot)
            if (!isUserMessage) {
                ProfileImage(
                    imageUrl = bot.fotoUrl,
                    size = 36
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Contenido del mensaje
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUserMessage) 16.dp else 4.dp,
                            bottomEnd = if (isUserMessage) 4.dp else 16.dp
                        )
                    )
                    .background(bubbleColor)
                    .padding(12.dp)
                    .clickable { 
                        if (!isUserMessage) {
                            onSpeakText(message.text)
                        }
                    }
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Avatar (solo para mensajes del usuario)
            if (isUserMessage) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileImage(
                    imageUrl = userProfile.fotoPerfilUri ?: "",
                    size = 36
                )
            }
        }
        
        // Hora del mensaje
        Text(
            text = formatMessageTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = if (isUserMessage) 0.dp else 48.dp,
                end = if (isUserMessage) 48.dp else 0.dp,
                top = 4.dp
            )
        )
    }
}

// Función para formatear la hora del mensaje
fun formatMessageTime(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    return String.format("%02d:%02d", hour, minute)
} 