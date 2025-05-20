package com.example.venusai.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.venusai.R
import com.example.venusai.data.BotRepository
import com.example.venusai.data.ChatRepository
import com.example.venusai.data.ThoughtRepository
import com.example.venusai.data.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.font.FontWeight
import coil.request.ImageRequest
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountConfigScreen(
    initialProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    scope: CoroutineScope? = null,
    context: android.content.Context? = null,
    themeMode: String = "auto",
    onThemeChange: (String) -> Unit = {}
) {
    var nombre by remember { mutableStateOf(initialProfile.nombre) }
    var apellidos by remember { mutableStateOf(initialProfile.apellidos) }
    var usuario by remember { mutableStateOf(initialProfile.usuario) }
    var fotoPerfilUri by remember { mutableStateOf(initialProfile.fotoPerfilUri) }
    var biografia by remember { mutableStateOf(initialProfile.biografia) }
    var intereses by remember { mutableStateOf(initialProfile.intereses) }
    var ubicacion by remember { mutableStateOf(initialProfile.ubicacion) }
    var isPremium by remember { mutableStateOf(initialProfile.isPremium) }
    var selectedTheme by remember { mutableStateOf(themeMode) }
    var showTermsAndConditions by remember { mutableStateOf(false) }
    val actualContext = LocalContext.current
    // Usar el contexto pasado como parámetro o el contexto local si es nulo
    val effectiveContext = context ?: actualContext

    // Estados de validación
    var isFormValid by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    fun validateForm() {
        isFormValid = nombre.isNotBlank() && apellidos.isNotBlank() && usuario.isNotBlank()
    }

    // Validar al cambiar valores
    LaunchedEffect(nombre, apellidos, usuario) {
        validateForm()
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoPerfilUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de perfil") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de información
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUri.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(fotoPerfilUri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }
            
            // Botón para cambiar foto de perfil
            TextButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Cambiar foto de perfil")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campos de perfil
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nombre")
                        Text("*", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Apellidos")
                        Text("*", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nombre de usuario")
                        Text("*", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = biografia,
                onValueChange = { 
                    // Limitar a 200 caracteres
                    if (it.length <= 200) biografia = it 
                },
                label = { Text("Biografía") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5
            )
            
            // Contador de caracteres para biografía
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${biografia.length}/200 caracteres, ${biografia.lines().size}/5 líneas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = intereses,
                onValueChange = { intereses = it },
                label = { Text("Intereses") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de Premium
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Usuario Premium",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isPremium,
                            onCheckedChange = { isPremium = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Activa el modo premium para disfrutar de la app sin anuncios y acceder a los 3 bots de IA exclusivos para usuarios premium.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Ventajas de ser Premium
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PremiumFeatureItem(
                            text = "Sin anuncios en toda la aplicación",
                            enabled = isPremium
                        )
                        PremiumFeatureItem(
                            text = "Acceso a 3 bots exclusivos de IA avanzada",
                            enabled = isPremium
                        )
                        PremiumFeatureItem(
                            text = "Respuestas más rápidas y completas",
                            enabled = isPremium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón de Términos y Condiciones
            OutlinedButton(
                onClick = { showTermsAndConditions = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Términos y Condiciones")
                }
            }
            
            // Diálogo de Términos y Condiciones
            if (showTermsAndConditions) {
                AlertDialog(
                    onDismissRequest = { showTermsAndConditions = false },
                    title = { Text("Términos y Condiciones") },
                    text = { 
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "POLÍTICA DE PRIVACIDAD Y PROTECCIÓN DE DATOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "En cumplimiento con las leyes de protección de datos, nos comprometemos a proteger su privacidad y sus datos personales.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "INFORMACIÓN QUE RECOPILAMOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Información que usted proporciona: nombre, apellidos, nombre de usuario, foto de perfil, biografía, intereses y ubicación.\n" +
                                "• Esta información es totalmente opcional y usted decide qué compartir.\n" +
                                "• Ninguna información es compartida con terceros sin su consentimiento explícito.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "USO DE SUS DATOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Su información se utiliza exclusivamente para proporcionar la experiencia personalizada dentro de la aplicación.\n" +
                                "• No compartimos sus datos con terceros para fines comerciales o publicitarios.\n" +
                                "• Todos los datos se almacenan localmente en su dispositivo.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "ELIMINACIÓN DE DATOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Cuando usted elimina su cuenta, todos sus datos personales son eliminados permanentemente.\n" +
                                "• Puede solicitar la eliminación de sus datos en cualquier momento utilizando la opción 'Eliminar cuenta'.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "SEGURIDAD",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Implementamos medidas de seguridad técnicas y organizativas para proteger sus datos personales.\n" +
                                "• Sus datos se almacenan de manera segura en su dispositivo.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTermsAndConditions = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Aceptar")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón eliminar cuenta
            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar cuenta")
            }
            
            // Diálogo de confirmación para eliminar cuenta
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Eliminar cuenta") },
                    text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.") },
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
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Selector de tema
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tema:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 16.dp)
                )
                
                DropdownMenuTheme(
                    selected = selectedTheme,
                    onThemeChange = {
                        selectedTheme = it
                        onThemeChange(it)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón Cancelar
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón Guardar
                Button(
                    onClick = {
                        showValidationErrors = true
                        if (isFormValid) {
                            onSave(UserProfile(
                                id = initialProfile.id,
                                nombre = nombre,
                                apellidos = apellidos,
                                usuario = usuario,
                                fotoPerfilUri = fotoPerfilUri,
                                biografia = biografia,
                                intereses = intereses,
                                ubicacion = ubicacion,
                                isPremium = isPremium
                            ))
                            showSaveSuccess = true
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }

        // Snackbar de éxito al guardar
        if (showSaveSuccess) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 64.dp),
                action = {
                    TextButton(onClick = { showSaveSuccess = false }) {
                        Text("OK")
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perfil guardado exitosamente")
                }
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(text: String, enabled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DropdownMenuTheme(selected: String, onThemeChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { expanded = true },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when (selected) {
                    "light" -> "Claro"
                    "dark" -> "Oscuro"
                    else -> "Automático"
                }
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        DropdownMenuItem(
            text = { Text("Automático") },
            onClick = { onThemeChange("auto"); expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Claro") },
            onClick = { onThemeChange("light"); expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Oscuro") },
            onClick = { onThemeChange("dark"); expanded = false }
        )
    }
}