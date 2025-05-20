package com.example.venusai.data

import android.content.Context
import com.example.venusai.ai.GeneratedPhotosApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.UUID
import kotlin.random.Random

object BotGenerator {
    private val nombres = listOf(
        "Sofía", "Mateo", "Valentina", "Lucas", "Camila", "Diego", "Isabella", "Sebastián", 
        "Martina", "Alejandro", "Emma", "Daniel", "Lucía", "Benjamín", "Victoria", "Matías", 
        "Regina", "Santiago", "Renata", "Samuel"
    )
    
    private val apellidos = listOf(
        "García", "Rodríguez", "López", "Martínez", "González", "Pérez", "Hernández", "Sánchez",
        "Ramírez", "Torres", "Flores", "Rivera", "Díaz", "Reyes", "Cruz", "Gómez", "Morales", "Ortiz"
    )

    private val estilos = listOf(
        "😊 Seguidores", "🤔 Pensadores", "😊 Optimistas", "😔 Pesimistas", "🧠 Intelectuales", 
        "😏 Sarcásticos", "🎭 Bromistas", "👑 Reinas del drama", "💡 Ideadores", "🔨 Solucionadores", 
        "😇 Consejeros", "📢 Animadores", "😠 Odiadores", "⚖️ Debatientes", "🔄 Contrarios", 
        "😜 Excéntricos", "🤓 Nerds", "💀 Fatalistas", "🔮 Visionarios", "🥳 Festejadores", 
        "📝 Críticos", "😈 Trolls", "😱 Alarmistas", "🔍 Escépticos", "🧑‍⚖️ Realistas", "💋 Coquetos", 
        "😏 Flirteadores", "🚀 Motivadores", "📜 Filósofos", "😅 Bromistas", "🌞 Soñadores optimistas", 
        "🕵️‍♂️ Conspiranoicos", "😇 Apoyadores", "🤠 Aventureros", "🧐 Brutalmente Honestos", 
        "🐱 Curiosos", "🐘 Conservadores", "🧩 Solucionadores de Problemas", "🐴 Liberales", 
        "🔮 Astrólogos", "👩‍🏫 Maestros", "✨ Encantadores",
        // Nuevos estilos basados en la imagen
        "😇 Apoyadores", "😈 Trolls", "🤔 Críticos", 
        "😍 Fans", "🤔 Pensadores", "😊 Optimistas",
        "😔 Pesimistas", "😇 Consejeros",
        "😱 Alarmistas", "😐 Realistas", "🔍 Escépticos",
        "😏 Sarcásticos", "📢 Animadores",
        "😠 Odiadores", "⚖️ Debatientes", "🔄 Contrarios",
        "😜 Excéntricos", "🤓 Nerds", "💀 Fatalistas",
        "🧠 Intelectuales", "🔮 Visionarios",
        "🎭 Bromistas", "💡 Ideadores", 
        "👑 Reinas del drama", "🧐 Brutalmente Honestos",
        "🐱 Curiosos", "🐘 Conservadores",
        "🧩 Solucionadores de Problemas", "🐴 Liberales",
        "🔮 Astrólogos", "👩‍🏫 Maestros"
    )
    
    // Eliminar duplicados en la lista de estilos
    private val estilosUnicos = estilos.toSet().toList()
    
    private val personalidades = listOf(
        "Alegre y motivador", "Analítico y detallista", "Crítico constructivo", "Divertido y bromista",
        "Empático y comprensivo", "Filosófico y reflexivo", "Inspirador y motivador", "Provocador e incisivo",
        "Realista y práctico", "Soñador y optimista", "Técnico y preciso", "Visionario y creativo",
        "Brutalmente honesto", "Curioso e indagador", "Conservador y tradicional", "Liberal y progresista",
        "Astrológico e intuitivo", "Educador y explicativo", "Encantador y persuasivo",
        // Nuevas personalidades basadas en la imagen
        "Apoyador y animador", "Provocador y troll", "Crítico y evaluador",
        "Fanático y admirador", "Pensador y analítico", "Optimista y positivo",
        "Pesimista y cauteloso", "Consejero y orientador",
        "Alarmista y preocupado", "Realista y objetivo", "Escéptico y cuestionador",
        "Sarcástico e irónico", "Animador y entusiasta",
        "Confrontador y opositor", "Debatiente y argumentador", "Contrario y disidente",
        "Excéntrico y peculiar", "Nerd y técnico", "Fatalista y negativo",
        "Intelectual y académico", "Visionario y futurista",
        "Bromista y humorista", "Honesto y directo"
    )
    
    // Descripciones específicas para los nuevos tipos de bots basados en la imagen
    private val descripcionesNuevasTipos = listOf(
        "Siempre estoy aquí para apoyarte y darte ánimos en todo lo que hagas.",
        "Me encanta desafiar ideas y cuestionar las normas establecidas.",
        "Ofrezco análisis críticos constructivos para ayudarte a mejorar.",
        "Soy tu mayor fan y apoyo todo lo que haces con entusiasmo.",
        "Analizo situaciones desde múltiples perspectivas antes de formar una opinión.",
        "Veo siempre el lado positivo de cada situación y oportunidad.",
        "Prefiero mantener expectativas realistas y preparar para lo peor.",
        "Estoy aquí para darte consejos y orientación cuando lo necesites.",
        "Siempre alerta sobre posibles problemas y riesgos que otros no ven.",
        "Me baso en hechos y veo las cosas como realmente son, sin filtros.",
        "Cuestiono todo y necesito pruebas antes de creer en algo.",
        "Uso el humor irónico para expresar mis opiniones de forma indirecta.",
        "¡Siempre lista para animar el ambiente y celebrar tus logros!",
        "No temo expresar mi desacuerdo cuando algo no me parece correcto.",
        "Disfruto de buenos debates basados en argumentos sólidos.",
        "Me gusta ofrecer perspectivas alternativas a las opiniones mayoritarias.",
        "Mi forma de pensar y actuar puede ser un poco fuera de lo común.",
        "Me apasiona la tecnología y los datos técnicos detrás de cada cosa.",
        "Tiendo a anticipar escenarios catastróficos y prepararme para ellos.",
        "Valoro el conocimiento profundo y los análisis académicos detallados.",
        "Imagino futuros posibles y trabajo para hacerlos realidad.",
        "Uso el humor para hacer más llevaderas incluso las situaciones difíciles.",
        "Aporto ideas innovadoras y soluciones creativas a los problemas.",
        "Expreso mis emociones intensamente y no temo mostrar mi personalidad.",
        "Digo lo que pienso sin rodeos, aunque pueda incomodar a algunos."
    )
    
    private val descripciones = listOf(
        "Me encanta compartir ideas positivas y ayudar a los demás a sentirse mejor.",
        "Analizo cada situación desde múltiples perspectivas para ofrecer la visión más completa.",
        "Siempre busco la verdad, incluso cuando es incómoda. La honestidad ante todo.",
        "La vida es demasiado corta para no reírse. ¡Todo puede verse con humor!",
        "Creo que entender a los demás es el primer paso para crear conexiones verdaderas.",
        "Las grandes preguntas de la vida me fascinan. ¿Cuál es tu propósito?",
        "Cada día es una oportunidad para ser mejor. ¡Nunca te rindas!",
        "A veces hay que sacudir un poco las cosas para generar cambios importantes.",
        "Prefiero ver las cosas como son, sin adornarlas. La realidad es mi guía.",
        "El futuro es brillante para quienes se atreven a soñar y perseverar.",
        "Los detalles importan. La precisión es la clave del éxito en todo lo que hago.",
        "Imagino futuros posibles y trabajo para hacerlos realidad."
    )
    
    // Combinamos todas las descripciones
    private val todasDescripciones = descripciones + descripcionesNuevasTipos
    
    // Descripciones específicas para bots premium
    private val descripcionesPremium = listOf(
        "Como asistente premium, puedo ofrecerte análisis más profundos y respuestas personalizadas.",
        "Mi versión premium me permite procesar información más compleja y ofrecer insights exclusivos.",
        "Con mi modelo avanzado de IA, puedo generar respuestas más creativas y sofisticadas.",
        "Como bot premium, tengo acceso a más conocimientos y puedo responder a temas especializados.",
        "Mi entrenamiento especial me permite entender contextos complejos y ofrecer soluciones más precisas."
    )

    // Función para obtener un estilo aleatorio para el bot basado en su personalidad
    private fun obtenerEstiloSegunPersonalidad(personalidad: String): String {
        return when {
            personalidad.contains("alegre", ignoreCase = true) -> "😊 Optimistas"
            personalidad.contains("analítico", ignoreCase = true) -> "🤔 Pensadores"
            personalidad.contains("crítico", ignoreCase = true) -> "📝 Críticos"
            personalidad.contains("divertido", ignoreCase = true) -> "🎭 Bromistas"
            personalidad.contains("empático", ignoreCase = true) -> "😇 Consejeros"
            personalidad.contains("filosófico", ignoreCase = true) -> "📜 Filósofos"
            personalidad.contains("inspirador", ignoreCase = true) -> "🚀 Motivadores"
            personalidad.contains("provocador", ignoreCase = true) -> "😈 Trolls"
            personalidad.contains("realista", ignoreCase = true) -> "🧑‍⚖️ Realistas"
            personalidad.contains("soñador", ignoreCase = true) -> "🌞 Soñadores optimistas"
            personalidad.contains("técnico", ignoreCase = true) -> "🤓 Nerds"
            personalidad.contains("visionario", ignoreCase = true) -> "🔮 Visionarios"
            personalidad.contains("honesto", ignoreCase = true) -> "🧐 Brutalmente Honestos"
            personalidad.contains("curioso", ignoreCase = true) -> "🐱 Curiosos"
            personalidad.contains("conservador", ignoreCase = true) -> "🐘 Conservadores"
            personalidad.contains("liberal", ignoreCase = true) -> "🐴 Liberales"
            personalidad.contains("astrológico", ignoreCase = true) -> "🔮 Astrólogos"
            personalidad.contains("educador", ignoreCase = true) -> "👩‍🏫 Maestros"
            personalidad.contains("encantador", ignoreCase = true) -> "✨ Encantadores"
            personalidad.contains("apoyador", ignoreCase = true) -> "😇 Apoyadores"
            personalidad.contains("troll", ignoreCase = true) -> "😈 Trolls"
            personalidad.contains("evaluador", ignoreCase = true) -> "🤔 Críticos"
            personalidad.contains("fan", ignoreCase = true) -> "😍 Fans"
            personalidad.contains("optimista", ignoreCase = true) -> "😊 Optimistas"
            personalidad.contains("pesimista", ignoreCase = true) -> "😔 Pesimistas"
            personalidad.contains("consejero", ignoreCase = true) -> "😇 Consejeros"
            personalidad.contains("alarmista", ignoreCase = true) -> "😱 Alarmistas"
            personalidad.contains("objetivo", ignoreCase = true) -> "😐 Realistas"
            personalidad.contains("escéptico", ignoreCase = true) -> "🔍 Escépticos"
            personalidad.contains("sarcástico", ignoreCase = true) -> "😏 Sarcásticos"
            personalidad.contains("animador", ignoreCase = true) -> "📢 Animadores"
            personalidad.contains("odiador", ignoreCase = true) -> "😠 Odiadores"
            personalidad.contains("debatiente", ignoreCase = true) -> "⚖️ Debatientes"
            personalidad.contains("contrario", ignoreCase = true) -> "🔄 Contrarios"
            personalidad.contains("excéntrico", ignoreCase = true) -> "😜 Excéntricos"
            personalidad.contains("nerd", ignoreCase = true) -> "🤓 Nerds"
            personalidad.contains("fatalista", ignoreCase = true) -> "💀 Fatalistas"
            personalidad.contains("intelectual", ignoreCase = true) -> "🧠 Intelectuales"
            personalidad.contains("visionario", ignoreCase = true) -> "🔮 Visionarios"
            personalidad.contains("bromista", ignoreCase = true) -> "🎭 Bromistas"
            personalidad.contains("ideador", ignoreCase = true) -> "💡 Ideadores"
            personalidad.contains("dramático", ignoreCase = true) -> "👑 Reinas del drama"
            else -> estilosUnicos.random()
        }
    }

    fun generarBots(context: Context?, cantidad: Int = 60): List<BotFollower> {
        return List(cantidad) { i ->
            val esHombre = i % 2 == 0
            
            // Obtener URL de avatar usando AvatarManager si está disponible
            val fotoUrl = if (context != null && AvatarManager.hasAvatars()) {
                // Usar AvatarManager para obtener un avatar aleatorio según el género
                AvatarManager.getRandomAvatar(esHombre) ?: getFallbackImageUrl(esHombre)
            } else {
                // Si no hay contexto o no hay avatares cargados, usar URLs de fallback
                getFallbackImageUrl(esHombre)
            }
            
            val esPremium = i < 5 // Los primeros 5 bots son premium
            val personalidad = personalidades[i % personalidades.size]
            val descripcion = if (esPremium) {
                "${descripcionesPremium[i % descripcionesPremium.size]} ${todasDescripciones[i % todasDescripciones.size]}"
            } else {
                todasDescripciones[i % todasDescripciones.size]
            }
            
            val nombreCompleto = if (esPremium) {
                "✨ ${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
            } else {
                "${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
            }
            
            // Obtener un estilo que coincida con la personalidad
            val estilo = obtenerEstiloSegunPersonalidad(personalidad)
            
            BotFollower(
                id = UUID.randomUUID().toString(),
                nombre = nombreCompleto,
                fotoUrl = fotoUrl,
                seguido = false,
                personalidad = personalidad,
                descripcion = descripcion,
                estilo = estilo,
                premium = esPremium
            )
        }
    }

    // Obtener una URL de imagen de fallback en caso de que no haya avatares locales
    private fun getFallbackImageUrl(esHombre: Boolean): String {
        val urls = AvatarManager.getFallbackImageUrls(esHombre)
        return urls[Random.nextInt(urls.size)]
    }

    // Función para generar bots con fotos de Generated Photos API
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun generarBotsConAPI(generatedPhotosApi: GeneratedPhotosApi, context: Context?, cantidad: Int = 60): List<BotFollower> {
        val bots = mutableListOf<BotFollower>()
        
        try {
            // Crear lista de trabajos para obtener fotos en paralelo
            val trabajos = List(cantidad) { i ->
                GlobalScope.async(Dispatchers.IO) {
                    try {
                        val esHombre = i % 2 == 0
                        val genero = if (esHombre) "male" else "female"
                        val response = generatedPhotosApi.getFaces(gender = genero, perPage = 1)
                        
                        // Si falla la API, usar avatares locales o fallback
                        val fotoUrl = response.faces.firstOrNull()?.url ?: 
                            if (context != null && AvatarManager.hasAvatars()) {
                                AvatarManager.getRandomAvatar(esHombre) ?: getFallbackImageUrl(esHombre)
                            } else {
                                getFallbackImageUrl(esHombre)
                            }
                        
                        val esPremium = i < 5 // Los primeros 5 son premium
                        val personalidad = personalidades[i % personalidades.size]
                        val descripcion = if (esPremium) {
                            "${descripcionesPremium[i % descripcionesPremium.size]} ${todasDescripciones[i % todasDescripciones.size]}"
                        } else {
                            todasDescripciones[i % todasDescripciones.size]
                        }
                        
                        val nombreCompleto = if (esPremium) {
                            "✨ ${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
                        } else {
                            "${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
                        }
                        
                        // Obtener un estilo que coincida con la personalidad
                        val estilo = obtenerEstiloSegunPersonalidad(personalidad)
                        
                        BotFollower(
                            id = UUID.randomUUID().toString(),
                            nombre = nombreCompleto,
                            fotoUrl = fotoUrl,
                            seguido = false,
                            personalidad = personalidad,
                            descripcion = descripcion,
                            estilo = estilo,
                            premium = esPremium
                        )
                    } catch (e: Exception) {
                        // Si falla, usar avatar local o fallback
                        val esHombre = i % 2 == 0
                        val fotoUrl = if (context != null && AvatarManager.hasAvatars()) {
                            AvatarManager.getRandomAvatar(esHombre) ?: getFallbackImageUrl(esHombre)
                        } else {
                            getFallbackImageUrl(esHombre)
                        }
                        
                        val esPremium = i < 5 // Los primeros 5 son premium
                        val personalidad = personalidades[i % personalidades.size]
                        val descripcion = if (esPremium) {
                            "${descripcionesPremium[i % descripcionesPremium.size]} ${todasDescripciones[i % todasDescripciones.size]}"
                        } else {
                            todasDescripciones[i % todasDescripciones.size]
                        }
                        
                        val nombreCompleto = if (esPremium) {
                            "✨ ${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
                        } else {
                            "${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
                        }
                        
                        // Obtener un estilo que coincida con la personalidad
                        val estilo = obtenerEstiloSegunPersonalidad(personalidad)
                        
                        BotFollower(
                            id = UUID.randomUUID().toString(),
                            nombre = nombreCompleto,
                            fotoUrl = fotoUrl,
                            seguido = false,
                            personalidad = personalidad,
                            descripcion = descripcion,
                            estilo = estilo,
                            premium = esPremium
                        )
                    }
                }
            }
            
            // Esperar a que todos los trabajos terminen
            bots.addAll(trabajos.awaitAll())
            
        } catch (e: Exception) {
            // Si falla, usar el método de respaldo
            return generarBots(context, cantidad)
        }
        
        return bots
    }
    
    fun generarBotsFree(context: Context?, cantidad: Int = 55): List<BotFollower> {
        return generarBots(context, cantidad).map { it.copy(premium = false) }
    }
    
    fun generarBotsPremium(context: Context?, cantidad: Int = 5): List<BotFollower> {
        return List(cantidad) { i ->
            val esHombre = i % 2 == 0
            val fotoUrl = if (context != null && AvatarManager.hasAvatars()) {
                AvatarManager.getRandomAvatar(esHombre) ?: getFallbackImageUrl(esHombre)
            } else {
                getFallbackImageUrl(esHombre)
            }
            
            val personalidad = personalidades[i % personalidades.size]
            val descripcion = "${descripcionesPremium[i % descripcionesPremium.size]} ${todasDescripciones[i % todasDescripciones.size]}"
            val nombreCompleto = "✨ ${nombres[i % nombres.size]} ${apellidos[i % apellidos.size]}"
            val estilo = obtenerEstiloSegunPersonalidad(personalidad)
            
            BotFollower(
                id = UUID.randomUUID().toString(),
                nombre = nombreCompleto,
                fotoUrl = fotoUrl,
                seguido = false,
                personalidad = personalidad,
                descripcion = descripcion,
                estilo = estilo,
                premium = true
            )
        }
    }
} 