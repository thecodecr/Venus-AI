package com.example.venusai.data

import android.content.Context
import android.util.Log
import java.io.File
import kotlin.random.Random

/**
 * Gestor de avatares que se encarga de cargar y asignar aleatoriamente avatares a los bots
 */
object AvatarManager {
    private const val TAG = "AvatarManager"
    private const val MALE_AVATAR_DIR = "avatars/male"
    private const val FEMALE_AVATAR_DIR = "avatars/female"
    
    // Listas para almacenar las rutas de los avatares
    private var maleAvatars = mutableListOf<String>()
    private var femaleAvatars = mutableListOf<String>()
    
    // Conjunto para hacer seguimiento de los avatares asignados para evitar repeticiones excesivas
    private val assignedMaleAvatars = mutableSetOf<String>()
    private val assignedFemaleAvatars = mutableSetOf<String>()
    
    /**
     * Inicializa el gestor de avatares cargando todas las imágenes disponibles
     */
    fun initialize(context: Context) {
        try {
            // Cargar avatares masculinos
            maleAvatars = context.assets.list(MALE_AVATAR_DIR)
                ?.map { "file:///android_asset/$MALE_AVATAR_DIR/$it" }
                ?.toMutableList() ?: mutableListOf()
            
            // Cargar avatares femeninos
            femaleAvatars = context.assets.list(FEMALE_AVATAR_DIR)
                ?.map { "file:///android_asset/$FEMALE_AVATAR_DIR/$it" }
                ?.toMutableList() ?: mutableListOf()
            
            Log.d(TAG, "Avatares cargados - Masculinos: ${maleAvatars.size}, Femeninos: ${femaleAvatars.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar avatares", e)
        }
    }
    
    /**
     * Obtiene un avatar aleatorio según el género
     * @param isMale true si el avatar es para un bot masculino, false para femenino
     * @return URL del avatar o null si no hay avatares disponibles
     */
    fun getRandomAvatar(isMale: Boolean): String? {
        val avatars = if (isMale) maleAvatars else femaleAvatars
        val assignedAvatars = if (isMale) assignedMaleAvatars else assignedFemaleAvatars
        
        if (avatars.isEmpty()) {
            Log.w(TAG, "No hay avatares disponibles para ${if (isMale) "hombres" else "mujeres"}")
            return null
        }
        
        // Si todos los avatares ya han sido asignados, limpiamos el conjunto para permitir reutilizaciones
        if (assignedAvatars.size >= avatars.size * 0.8) {
            assignedAvatars.clear()
        }
        
        // Intentar encontrar un avatar no asignado
        val availableAvatars = avatars.filterNot { assignedAvatars.contains(it) }
        val avatar = if (availableAvatars.isNotEmpty()) {
            // Seleccionar un avatar no utilizado
            availableAvatars[Random.nextInt(availableAvatars.size)]
        } else {
            // Seleccionar cualquier avatar si todos están en uso
            avatars[Random.nextInt(avatars.size)]
        }
        
        // Registrar el avatar como asignado
        assignedAvatars.add(avatar)
        
        return avatar
    }
    
    /**
     * Verifica si ya hay avatares cargados
     */
    fun hasAvatars(): Boolean {
        return maleAvatars.isNotEmpty() || femaleAvatars.isNotEmpty()
    }
    
    /**
     * Obtiene las URLs de las imágenes de fallback en caso de que no haya avatares locales
     */
    fun getFallbackImageUrls(isMale: Boolean): List<String> {
        return if (isMale) {
            listOf(
                "https://i.imgur.com/5YB7aPt.jpg",
                "https://i.imgur.com/YeYADxE.jpg",
                "https://i.imgur.com/UXbIpKm.jpg",
                "https://i.imgur.com/svM3on3.jpg",
                "https://i.imgur.com/NKUY3HY.jpg"
            )
        } else {
            listOf(
                "https://i.imgur.com/CiHmUiO.jpg",
                "https://i.imgur.com/ij9oXnH.jpg",
                "https://i.imgur.com/jMrj2bg.jpg",
                "https://i.imgur.com/rGvGVIb.jpg",
                "https://i.imgur.com/RQSVEBj.jpg"
            )
        }
    }
} 