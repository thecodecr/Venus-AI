package com.example.venusai.ui

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venusai.R
import com.example.venusai.data.UserProfile
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun LoginScreen(
    onLoginComplete: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Firebase Auth instance
    val auth = remember { Firebase.auth }
    
    // Identity API client
    val oneTapClient = remember { Identity.getSignInClient(context) }
    
    // Estados para la animación
    val titleState = remember { MutableTransitionState(false) }
    val logoState = remember { MutableTransitionState(false) }
    val descriptionState = remember { MutableTransitionState(false) }
    val buttonState = remember { MutableTransitionState(false) }
    var showManualLogin by remember { mutableStateOf(false) }
    
    // Configuración de Firebase Auth Sign In
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("318586427804-327q7i0oghseqk1l8o2egmlj67ajtouh.apps.googleusercontent.com") // Cliente web del google-services.json
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }
    
    // Función para continuar sin inicio de sesión (modo de emergencia)
    fun continueWithoutLogin() {
        // Crear un perfil de usuario temporal
        val temporalProfile = UserProfile(
            id = "user_${UUID.randomUUID()}",
            nombre = "Usuario",
            apellidos = "Temporal",
            usuario = "usuario_temp",
            fotoPerfilUri = null,
            biografia = "Usuario de Venus AI",
            intereses = "",
            ubicacion = ""
        )
        onLoginComplete(temporalProfile)
    }
    
    // Launcher para iniciar sesión con Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    // Autenticar con Firebase usando el token
                    scope.launch {
                        try {
                            isLoading = true
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            val authResult = auth.signInWithCredential(firebaseCredential).await()
                            
                            // Usuario autenticado, crear perfil
                            val firebaseUser = authResult.user
                            if (firebaseUser != null) {
                                val newProfile = UserProfile(
                                    id = firebaseUser.uid,
                                    nombre = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "Usuario",
                                    apellidos = firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                                    usuario = firebaseUser.email?.split("@")?.firstOrNull() ?: "usuario",
                                    fotoPerfilUri = firebaseUser.photoUrl?.toString(),
                                    biografia = "Usuario de Venus AI",
                                    intereses = "",
                                    ubicacion = ""
                                )
                                
                                // Pequeña pausa para la animación
                                delay(500)
                                isLoading = false
                                onLoginComplete(newProfile)
                            } else {
                                isLoading = false
                                errorMessage = "No se pudo obtener la información de usuario"
                                showManualLogin = true
                            }
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Error de autenticación: ", e)
                            isLoading = false
                            errorMessage = "Error de autenticación: ${e.message}"
                            showManualLogin = true
                        }
                    }
                } else {
                    errorMessage = "No se pudo obtener el token de identificación"
                    showManualLogin = true
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Error de Google Sign In: ", e)
                errorMessage = "Error de Google Sign In: ${e.message}"
                showManualLogin = true
            }
        } else {
            errorMessage = "Inicio de sesión cancelado"
            showManualLogin = true
        }
    }
    
    // Función para iniciar el proceso de login con Google
    fun signInWithGoogle() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val result = oneTapClient.beginSignIn(signInRequest).await()
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(intentSenderRequest)
                isLoading = false
            } catch (e: Exception) {
                Log.e("LoginScreen", "No se pudo iniciar sesión con Google: ", e)
                isLoading = false
                if (e is ApiException && e.statusCode == CommonStatusCodes.CANCELED) {
                    errorMessage = "Servicio no disponible"
                } else {
                    errorMessage = "No se pudo iniciar sesión con Google: ${e.message}"
                }
                showManualLogin = true
            }
        }
    }
    
    // Animación secuencial de elementos
    LaunchedEffect(Unit) {
        logoState.targetState = true
        delay(300)
        titleState.targetState = true
        delay(300)
        descriptionState.targetState = true
        delay(300)
        buttonState.targetState = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo animado
            AnimatedVisibility(
                visibleState = logoState,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.venus_logo),
                    contentDescription = "Venus AI Logo",
                    modifier = Modifier.size(140.dp),
                    tint = Color.Unspecified
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título animado
            AnimatedVisibility(
                visibleState = titleState,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Text(
                    text = "Bienvenido a Venus AI",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Descripción animada
            AnimatedVisibility(
                visibleState = descriptionState,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Inicia sesión para continuar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Conecta con tu cuenta de Google para disfrutar de una experiencia social única con IA",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Botón de inicio de sesión con Google
            AnimatedVisibility(
                visibleState = buttonState,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                OutlinedButton(
                    onClick = { 
                        if (!isLoading) {
                            signInWithGoogle()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Continuar con Google",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            
            if (showManualLogin) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { continueWithoutLogin() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continuar sin iniciar sesión")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mensaje de error
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                
                // Opción para continuar sin iniciar sesión si hay error
                if (!showManualLogin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showManualLogin = true }
                    ) {
                        Text("Mostrar opciones alternativas")
                    }
                }
            }
        }
    }
} 