package com.example.venusai

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.venusai.ai.OpenRouterMessage
import com.example.venusai.ai.OpenRouterRequest
import com.example.venusai.ai.OpenRouterApi
import com.example.venusai.data.AvatarManager
import com.example.venusai.data.BotComment
import com.example.venusai.data.BotFollower
import com.example.venusai.data.BotGenerator
import com.example.venusai.data.BotRepository
import com.example.venusai.data.ChatMessage
import com.example.venusai.data.ChatRepository
import com.example.venusai.data.CommentAIType
import com.example.venusai.data.Thought
import com.example.venusai.data.ThoughtRepository
import com.example.venusai.data.UserPreferences
import com.example.venusai.data.UserProfile
import com.example.venusai.ui.AccountConfigScreen
import com.example.venusai.ui.BotListScreen
import com.example.venusai.ui.ChatScreen
import com.example.venusai.ui.ProfileScreen
import com.example.venusai.ui.theme.VenusAITheme
import com.example.venusai.ui.SplashScreen
import com.example.venusai.ui.OnboardingScreen
import com.example.venusai.ui.WelcomeScreen
import com.example.venusai.ui.FeedScreen
import com.example.venusai.ui.LoginScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import android.net.Uri
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdSize
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup.LayoutParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    // API Keys constantes
    companion object {
        // OpenRouter.ai API Key
        private const val OPENROUTER_API_KEY = "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" 
        
        // Modelos disponibles en OpenRouter.ai
        // Puedes cambiar el DEFAULT_MODEL a cualquiera de estos según tus preferencias de calidad/costo
        private const val MODEL_GPT35 = "openai/gpt-3.5-turbo"        // Económico y rápido
        private const val MODEL_GPT4 = "openai/gpt-4"                 // Alta calidad pero costoso
        private const val MODEL_CLAUDE3 = "anthropic/claude-3-haiku"  // Buen equilibrio calidad/velocidad
        private const val MODEL_LLAMA3 = "meta-llama/llama-3-8b-instruct" // Open source, buena calidad
        private const val MODEL_GEMINI = "google/gemini-pro"          // Buena calidad general
        
        // Modelo predeterminado a usar
        private const val DEFAULT_MODEL = MODEL_GPT35
        
        // AdMob
        private const val AD_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Banner de prueba
        private const val AD_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712" // Intersticial de prueba
        
        // Claves de OpenRouter y Generated Photos API 
        const val GENERATED_PHOTOS_API_KEY = "..."
    }
    
    // Variables para la gestión de anuncios
    private var interstitialAd: InterstitialAd? = null
    private var lastAdShownTime = 0L
    private var adCounter = 0
    
    // Tiempo mínimo entre anuncios (en ms)
    private val MIN_TIME_BETWEEN_ADS = 5 * 60 * 1000 // 5 minutos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar el AvatarManager
        AvatarManager.initialize(this)
        
        // Inicializar interstitial ad
        MobileAds.initialize(this)
        
        // Configurar el tema basado en las preferencias guardadas
        lifecycleScope.launch {
            UserPreferences.getDarkMode(this@MainActivity).collect { isDarkMode ->
                if (isDarkMode != null) {
                    // Aplicar tema utilizando recursos nativos de Android
                    if (isDarkMode) {
                        setTheme(android.R.style.ThemeOverlay_Material_Dark)
                    } else {
                        setTheme(android.R.style.ThemeOverlay_Material_Light)
                    }
                }
            }
        }
        
        // Cargar el primer anuncio intersticial
        loadInterstitialAd()
        
        setContent {
            var isDarkTheme = isSystemInDarkTheme() // Valor fijo basado en el sistema
            VenusAITheme(darkTheme = isDarkTheme) {
                MainContent(isDarkTheme = isDarkTheme)
            }
        }
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            this,
            AD_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    setupInterstitialAdCallbacks()
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    // Intentar cargar de nuevo después de un tiempo
                    lifecycleScope.launch {
                        delay(60000) // 1 minuto
                        loadInterstitialAd()
                    }
                }
            }
        )
    }
    
    private fun setupInterstitialAdCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // El anuncio fue cerrado, cargar uno nuevo
                interstitialAd = null
                loadInterstitialAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Falló la muestra del anuncio
                interstitialAd = null
                loadInterstitialAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                // Anuncio mostrado correctamente
                lastAdShownTime = System.currentTimeMillis()
            }
        }
    }
    
    // Función para mostrar anuncio intersticial si el usuario no es premium
    fun showInterstitialIfNeeded(isPremiumUser: Boolean) {
        if (isPremiumUser) return // No mostrar anuncios a usuarios premium
        
        adCounter++
        
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastAdShownTime
        
        // Mostrar anuncio cada 5 acciones si han pasado al menos 5 minutos desde el último
        if (adCounter >= 5 && timeSinceLastAd >= MIN_TIME_BETWEEN_ADS) {
            interstitialAd?.show(this) ?: loadInterstitialAd()
            adCounter = 0
        }
    }
    
    @Composable
    fun BannerAdView(isPremiumUser: Boolean) {
        if (!isPremiumUser) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    // Crear el AdView con el identificador del anuncio en el constructor
                    val adView = AdView(context)
                    adView.setAdSize(AdSize.BANNER)
                    adView.setAdUnitId(AD_BANNER_ID)
                    adView.layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                    )
                    adView.loadAd(AdRequest.Builder().build())
                    adView
                }
            )
        }
    }
    
    @Composable
    fun MainContent(isDarkTheme: Boolean) {
        val scope = rememberCoroutineScope()
        var currentScreen by remember { mutableStateOf("splash") }
        var userProfile by remember { mutableStateOf(UserProfile()) }
        var bots by remember { mutableStateOf(BotGenerator.generarBots(this, 10)) }
        var thoughts by remember { mutableStateOf(listOf<Thought>()) }
        var comments by remember { mutableStateOf(mapOf<String, List<BotComment>>()) }
        var chatBot by remember { mutableStateOf<BotFollower?>(null) }
        var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
        var chats by remember { mutableStateOf(mapOf<String, List<ChatMessage>>()) }
        var tipoIA by remember { mutableStateOf<CommentAIType?>(CommentAIType.POSITIVO) }  // Valor por defecto
        var showSplash by remember { mutableStateOf(true) }
        var showOnboarding by remember { mutableStateOf(true) }
        var isPremiumUser by remember { mutableStateOf(false) }
        var showThemeSelector by remember { mutableStateOf(false) }
        // Variables para la funcionalidad de red social
        var selectedBots by remember { mutableStateOf(listOf<BotFollower>()) }
        // Estado para controlar el login
        var isUserLoggedIn by remember { mutableStateOf(false) }
        
        val context = LocalContext.current

        // Verificar si el usuario está logueado
        LaunchedEffect(Unit) {
            UserPreferences.isUserLoggedIn(context).collect { loggedIn ->
                isUserLoggedIn = loggedIn
            }
        }

        // Verificar si el usuario es premium y cargar la configuración guardada
        LaunchedEffect(Unit) {
            // Carga desde DataStore de manera persistente
            UserPreferences.isPremiumUser(context).collect { isPremium ->
                isPremiumUser = isPremium
            }
        }
        
        // Crear API con la API key definida en el companion object
        val openRouterApi = remember { OpenRouterApi.create(OPENROUTER_API_KEY) }

        // Cargar datos al iniciar
        LaunchedEffect(Unit) {
            // Cargar pensamientos guardados
            ThoughtRepository.getThoughts(context).collect { savedThoughts ->
                thoughts = savedThoughts
            }
            
            // Cargar bots guardados o generar nuevos
            BotRepository.getBots(context).collect { savedBots ->
                if (savedBots.isNotEmpty()) {
                    bots = savedBots
                } else {
                    // Generar nuevos bots con las fotos precargadas
                    val nuevosBots = BotGenerator.generarBots(context, 60) // Actualizamos a 60 bots para incluir todos los tipos
                    bots = nuevosBots
                    // Guardar bots en DataStore para persistencia
                    scope.launch {
                        BotRepository.saveBots(context, nuevosBots)
                    }
                }
            }
            
            // Cargar chats guardados
            ChatRepository.getChats(context).collect { savedChats ->
                chats = savedChats as Map<String, List<ChatMessage>>
            }
            
            // Cargar perfil de usuario guardado
            UserPreferences.getUserProfile(context).collect { savedProfile ->
                userProfile = savedProfile
                // Usar el valor del perfil para actualizar el estado premium
                isPremiumUser = savedProfile.isPremium
                
                // Guardamos el perfil para asegurar que todos los campos estén sincronizados
                scope.launch {
                    UserPreferences.saveUserProfile(context, savedProfile)
                }
            }
            
            // Cargar comentarios guardados
            ThoughtRepository.getComments(context).collect { savedComments ->
                comments = savedComments
            }
            
            // Cargar configuración de tipo de IA
            UserPreferences.getAIType(context).collect { savedType ->
                tipoIA = savedType ?: CommentAIType.POSITIVO
            }
        }

        // Verificar si mostrar onboarding
        LaunchedEffect(Unit) {
            UserPreferences.isOnboardingShown(context).collect { shown ->
                showOnboarding = !shown
                if (shown && !isUserLoggedIn) {
                    // Si ya se mostró onboarding pero no ha iniciado sesión, ir a login
                    currentScreen = "login"
                } else if (shown && isUserLoggedIn) {
                    // Si ya se mostró onboarding y está logueado, ir a la pantalla de perfil
                    currentScreen = "profile"
                }
            }
        }

        // Función para agregar un nuevo pensamiento (tweet) y generar comentarios de bots
        suspend fun addThought(text: String, imageUri: Uri?) {
            val newThought = Thought(
                id = UUID.randomUUID().toString(),
                texto = text,
                timestamp = System.currentTimeMillis(),
                imageUri = imageUri?.toString()
            )
            
            thoughts = listOf(newThought) + thoughts
            ThoughtRepository.saveThoughts(context, thoughts)
            
            // Generar comentarios automáticos de bots
            // Determinar qué bots harán comentarios (bots seguidos o una selección aleatoria si no hay suficientes)
            val commentingBots = bots.filter { it.seguido }.takeIf { it.isNotEmpty() } 
                ?: bots.shuffled().take(3)
            
            val newComments = mutableListOf<BotComment>()
            
            for (bot in commentingBots) {
                try {
                    // Generar un prompt basado en la personalidad del bot
                    val prompt = generateCommentPrompt(bot.nombre, bot.personalidad, text)
                    
                    // Crear el request para OpenRouter
                    val request = OpenRouterRequest(
                        model = DEFAULT_MODEL,
                        messages = listOf(
                            OpenRouterMessage("system", "Eres un asistente que responde brevemente como si fueras un usuario de una red social."),
                            OpenRouterMessage("user", prompt)
                        ),
                        max_tokens = 100,
                        temperature = 0.7f
                    )
                    
                    // Obtener respuesta de la API (con manejo de errores)
                    val commentText = try {
                        val response = openRouterApi.chat(request)
                        response.choices.firstOrNull()?.message?.content?.trim() ?: 
                            "¡Me encanta esta publicación!"
                    } catch (e: Exception) {
                        // Si falla la API, usar un comentario predefinido basado en la personalidad
                        when {
                            bot.personalidad.contains("alegre", ignoreCase = true) -> 
                                "¡Esto es genial! 😄 Me encanta ver tu contenido."
                            bot.personalidad.contains("crítico", ignoreCase = true) -> 
                                "Interesante perspectiva, aunque hay algunos puntos a considerar..."
                            bot.personalidad.contains("empático", ignoreCase = true) -> 
                                "Puedo entender cómo te sientes. Gracias por compartir."
                            else -> "Gracias por compartir esto, muy interesante."
                        }
                    }
                    
                    // Crear y añadir el comentario
                    val comment = BotComment(
                        id = UUID.randomUUID().toString(),
                        bot = bot,
                        texto = commentText
                    )
                    
                    newComments.add(comment)
                    
                    // Esperar un poco entre solicitudes para evitar límites de rate
                    delay(500)
                } catch (e: Exception) {
                    // Registrar el error pero continuar con el siguiente bot
                    e.printStackTrace()
                }
            }
            
            // Actualizar los comentarios en el repositorio
            val updatedComments = comments.toMutableMap()
            updatedComments[newThought.id] = newComments
            comments = updatedComments
            ThoughtRepository.saveComments(context, updatedComments)
        }
        
        // Estructura principal
        if (showSplash) {
            SplashScreen(onFinish = { showSplash = false })
        } else if (showOnboarding) {
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        UserPreferences.setOnboardingShown(context, true)
                    }
                    showOnboarding = false
                    currentScreen = "login"
                }
            )
        } else {
            if (currentScreen == "login") {
                LoginScreen(
                    onLoginComplete = { userProf ->
                        userProfile = userProf
                        scope.launch {
                            UserPreferences.saveUserProfile(context, userProf)
                            // Marcar al usuario como logueado
                            UserPreferences.setUserLoggedIn(context, true)
                        }
                        isUserLoggedIn = true
                        currentScreen = "profile"
                    }
                )
            } else if (currentScreen == "welcome") {
                WelcomeScreen(
                    onLoginComplete = { userProf ->
                        userProfile = userProf
                        scope.launch {
                            UserPreferences.saveUserProfile(context, userProf)
                        }
                        currentScreen = "profile"
                    }
                )
            } else {
                Scaffold(
                    bottomBar = {
                        Column {
                            // Banner de anuncios en la parte inferior
                            BannerAdView(isPremiumUser = isPremiumUser)
                            
                            // Barra de navegación inferior mejorada
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                                    label = { Text("Inicio") },
                                    selected = currentScreen == "feed",
                                    onClick = { 
                                        if (currentScreen != "feed") {
                                            currentScreen = "feed"
                                            showInterstitialIfNeeded(isPremiumUser)
                                        }
                                    }
                                )
                                
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                    label = { Text("Perfil") },
                                    selected = currentScreen == "profile" || currentScreen == "account_config",
                                    onClick = { 
                                        if (currentScreen != "profile") {
                                            currentScreen = "profile"
                                            showInterstitialIfNeeded(isPremiumUser)
                                        }
                                    }
                                )
                                
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Bots") },
                                    label = { Text("Bots") },
                                    selected = currentScreen == "bots",
                                    onClick = { 
                                        if (currentScreen != "bots") {
                                            currentScreen = "bots"
                                            showInterstitialIfNeeded(isPremiumUser)
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                    ) {
                        when (currentScreen) {
                            "feed" -> {
                                FeedScreen(
                                    thoughts = thoughts,
                                    comments = comments,
                                    userProfile = userProfile,
                                    selectedBots = bots.filter { it.seguido },
                                    openRouterApi = openRouterApi,
                                    onAddThought = { text, imageUri ->
                                        scope.launch {
                                            addThought(text, imageUri)
                                            // Mostrar anuncio ocasionalmente
                                            showInterstitialIfNeeded(isPremiumUser)
                                        }
                                    },
                                    onLikeThought = { thoughtId ->
                                        scope.launch {
                                            ThoughtRepository.likeThought(context, thoughtId, userProfile.id)
                                        }
                                    },
                                    onRetweetThought = { thoughtId ->
                                        scope.launch {
                                            ThoughtRepository.retweetThought(context, thoughtId, userProfile.id, "")
                                        }
                                    },
                                    onBack = {
                                        currentScreen = "profile"
                                    },
                                    scope = scope,
                                    model = DEFAULT_MODEL,
                                    apiKey = OPENROUTER_API_KEY
                                )
                            }
                            "bots" -> {
                                BotListScreen(
                                    bots = bots,
                                    onBotClick = { selectedBot ->
                                        // Verificar si el bot es premium y el usuario no lo es
                                        if (selectedBot.premium && !isPremiumUser) {
                                            // Mostrar mensaje o diálogo indicando que necesita ser premium
                                            // Para simplicidad, no implementamos el diálogo completo aquí
                                            scope.launch {
                                                // Redirigir a la pantalla de configuración para activar premium
                                                currentScreen = "account_config"
                                            }
                                        } else {
                                            chatBot = selectedBot
                                            // Cargar mensajes existentes para este bot
                                            chatMessages = chats[selectedBot.id] ?: listOf()
                                            
                                            currentScreen = "chat"
                                        }
                                    },
                                    onBack = {
                                        currentScreen = "profile"
                                    },
                                    isPremiumUser = isPremiumUser
                                )
                            }
                            "chat" -> {
                                if (chatBot != null) {
                                    ChatScreen(
                                        bot = chatBot!!,
                                        messages = chatMessages,
                                        onSend = { userMessage ->
                                            // Si el bot es premium y el usuario no, redireccionar a configuración
                                            if (chatBot!!.premium && !isPremiumUser) {
                                                scope.launch {
                                                    currentScreen = "account_config"
                                                }
                                                return@ChatScreen
                                            }
                                            
                                            // Añadir mensaje del usuario
                                            val newUserMessage = ChatMessage(
                                                id = UUID.randomUUID().toString(),
                                                text = userMessage,
                                                isUserMessage = true,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            
                                            chatMessages = chatMessages + newUserMessage
                                            
                                            // Guardar mensaje en el almacenamiento
                                            val botId = chatBot!!.id
                                            val updatedChats = chats.toMutableMap()
                                            updatedChats[botId] = chatMessages
                                            
                                            scope.launch {
                                                ChatRepository.saveChats(context, updatedChats)
                                                chats = updatedChats
                                                
                                                // Generar respuesta de la IA (en segundo plano)
                                                val respuesta = obtenerRespuestaIAParaChat(userMessage, chatBot!!)
                                                
                                                // Añadir mensaje de la IA
                                                val aiMessage = ChatMessage(
                                                    id = UUID.randomUUID().toString(),
                                                    text = respuesta,
                                                    isUserMessage = false,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                
                                                chatMessages = chatMessages + aiMessage
                                                
                                                // Actualizar chats
                                                updatedChats[botId] = chatMessages
                                                ChatRepository.saveChats(context, updatedChats)
                                                chats = updatedChats
                                                
                                                // Mostrar anuncio ocasionalmente después de varias conversaciones
                                                if (!isPremiumUser) {
                                                    showInterstitialIfNeeded(isPremiumUser)
                                                }
                                            }
                                        },
                                        onBack = {
                                            currentScreen = "bots"
                                        }
                                    )
                                }
                            }
                            "account_config" -> {
                                AccountConfigScreen(
                                    initialProfile = userProfile,
                                    onSave = { updatedProfile ->
                                        // Actualizar el estado premium en UserPreferences
                                        if (updatedProfile.isPremium != userProfile.isPremium) {
                                            scope.launch {
                                                UserPreferences.setPremiumUser(context, updatedProfile.isPremium)
                                            }
                                            isPremiumUser = updatedProfile.isPremium
                                        }
                                        
                                        userProfile = updatedProfile
                                        // Guardar en preferencias con persistencia garantizada
                                        scope.launch {
                                            UserPreferences.saveUserProfile(context, updatedProfile)
                                        }
                                        currentScreen = "profile"
                                    },
                                    onDelete = {
                                        // Resetear datos asegurando que se guarden completamente
                                        scope.launch {
                                            UserPreferences.saveUserProfile(context, UserProfile())
                                            UserPreferences.setOnboardingShown(context, false)
                                            UserPreferences.setPremiumUser(context, false)
                                            UserPreferences.setUserLoggedIn(context, false)
                                            BotRepository.saveBots(context, emptyList())
                                            ChatRepository.saveChats(context, emptyMap())
                                            ThoughtRepository.saveThoughts(context, emptyList())
                                            ThoughtRepository.saveComments(context, emptyMap())
                                            
                                            // Redirigir a la pantalla de bienvenida
                                            showOnboarding = true
                                            currentScreen = "welcome"
                                        }
                                    },
                                    onCancel = {
                                        currentScreen = "profile"
                                    },
                                    themeMode = if (isDarkTheme) "dark" else "light",
                                    onThemeChange = { newThemeMode ->
                                        val newDarkMode = newThemeMode == "dark"
                                        scope.launch {
                                            UserPreferences.setDarkMode(context, newDarkMode)
                                            // Aplicar el cambio de tema
                                            setDarkTheme(newDarkMode)
                                        }
                                    }
                                )
                            }
                            else -> {
                                // Pantalla por defecto
                                ProfileScreen(
                                    userProfile = userProfile,
                                    followers = bots.filter { !it.seguido },
                                    following = bots.filter { it.seguido },
                                    isPremiumUser = isPremiumUser,
                                    onFollowToggle = { bot ->
                                        // Si el bot es premium y el usuario no es premium, redirigir a configuración
                                        if (bot.premium && !isPremiumUser) {
                                            currentScreen = "account_config"
                                            return@ProfileScreen
                                        }
                                        
                                        bots = bots.map { 
                                            if (it.id == bot.id) it.copy(seguido = !it.seguido) else it 
                                        }
                                        scope.launch {
                                            BotRepository.saveBots(context, bots)
                                            // Mostrar anuncio ocasionalmente después de varias interacciones
                                            showInterstitialIfNeeded(isPremiumUser)
                                        }
                                    },
                                    onToggleTheme = {
                                        scope.launch {
                                            UserPreferences.setDarkMode(context, !isDarkTheme)
                                        }
                                    },
                                    isDarkTheme = isDarkTheme,
                                    onEditProfile = {
                                        currentScreen = "account_config"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Función para obtener respuesta de la IA para el chat
    private suspend fun obtenerRespuestaIAParaChat(
        userMessage: String,
        bot: BotFollower
    ): String {
        try {
            // Construir prompt contextualizado para la personalidad del bot
            val prompt = """
                Eres ${bot.nombre}, un bot de IA con la siguiente personalidad: ${bot.personalidad}.
                ${if (bot.descripcion.isNotEmpty()) "Descripción adicional: ${bot.descripcion}" else ""}
                
                Responde al siguiente mensaje del usuario de manera conversacional, manteniéndote en personaje
                y usando un tono que coincida con tu personalidad definida.
                
                Mensaje del usuario: "$userMessage"
                
                Tu respuesta:
            """.trimIndent()
            
            // Simulamos una respuesta básica según la personalidad
            // Esto evita tener que usar la API de OpenRouter que podría estar dando problemas
            return when {
                bot.personalidad.lowercase().contains("alegre") || 
                bot.personalidad.lowercase().contains("divertido") || 
                bot.personalidad.lowercase().contains("positivo") -> 
                    "¡Me encanta tu mensaje! 😄 ${respuestasAlegres.random()}"
                bot.personalidad.lowercase().contains("analítico") || 
                bot.personalidad.lowercase().contains("técnico") || 
                bot.personalidad.lowercase().contains("detallista") -> 
                    "Analizando tu mensaje... ${respuestasAnaliticas.random()}"
                bot.personalidad.lowercase().contains("crítico") || 
                bot.personalidad.lowercase().contains("constructivo") ->
                    "Interesante perspectiva. ${respuestasCriticas.random()}"
                bot.personalidad.lowercase().contains("empático") || 
                bot.personalidad.lowercase().contains("comprensivo") ->
                    "Entiendo cómo te sientes. ${respuestasEmpaticas.random()}"
                else -> "Gracias por tu mensaje. Me gustaría saber más sobre lo que piensas."
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return "Lo siento, estoy teniendo problemas para responder en este momento. ¿Podemos intentarlo de nuevo?"
        }
    }
    
    // Respuestas predefinidas para diferentes personalidades
    private val respuestasAlegres = listOf(
        "¡Eso suena genial! Siempre es bueno mantener una actitud positiva.",
        "¡Me encanta tu energía! Sigamos conversando con este entusiasmo.",
        "¡Qué divertido! Me alegra mucho que compartas esto conmigo.",
        "¡Fantástico! Eso es exactamente el tipo de cosas que me animan el día."
    )
    
    private val respuestasAnaliticas = listOf(
        "Si consideramos todos los factores, hay varios aspectos importantes a tener en cuenta.",
        "Desde un punto de vista lógico, podríamos analizar esto en diferentes niveles.",
        "Los datos sugieren que hay múltiples variables que influyen en esta situación.",
        "Evaluando objetivamente, hay pros y contras que debemos considerar cuidadosamente."
    )
    
    private val respuestasCriticas = listOf(
        "Es importante señalar que hay áreas que podrían mejorarse.",
        "Si me permites ser sincero, hay algunos puntos que merecen una revisión más profunda.",
        "Aprecio tu perspectiva, aunque creo que podríamos reconsiderar algunos aspectos.",
        "Siendo objetivo, veo tanto puntos fuertes como oportunidades de mejora."
    )
    
    private val respuestasEmpaticas = listOf(
        "Puedo imaginar cómo te sientes, y quiero que sepas que te apoyo.",
        "Es completamente normal sentirse así, y tus emociones son válidas.",
        "Estoy aquí para escucharte y acompañarte en este proceso.",
        "A veces las situaciones difíciles nos ayudan a crecer. Estoy contigo en esto."
    )
    
    // Función para generar un prompt para comentarios de IA basado en la personalidad del bot
    private fun generateCommentPrompt(botName: String, botPersonality: String, postContent: String): String {
        return """
            Eres ${botName}, un bot con la siguiente personalidad: ${botPersonality}.
            
            Responde con un breve comentario natural y conversacional a la siguiente publicación:
            "${postContent}"
            
            Tu comentario debe reflejar tu personalidad y no debe superar los 100 caracteres.
            No uses hashtags ni emojis excesivos.
            Responde como si estuvieras en una red social, de manera casual y genuina.
        """.trimIndent()
    }

    private fun setDarkTheme(isDarkTheme: Boolean) {
        // Usar recursos nativos de Android en lugar de AppCompatDelegate
        if (isDarkTheme) {
            setTheme(android.R.style.ThemeOverlay_Material_Dark)
        } else {
            setTheme(android.R.style.ThemeOverlay_Material_Light)
        }
    }
}