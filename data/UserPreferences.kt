package com.example.venusai.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object UserPreferences {
    private val Context.dataStore by preferencesDataStore(name = "user_prefs")
    val AI_TYPE_KEY = stringPreferencesKey("ai_type")
    val NOMBRE_KEY = stringPreferencesKey("nombre")
    val APELLIDOS_KEY = stringPreferencesKey("apellidos")
    val USUARIO_KEY = stringPreferencesKey("usuario")
    val FOTO_KEY = stringPreferencesKey("foto_perfil")
    val BIOGRAFIA_KEY = stringPreferencesKey("biografia")
    val INTERESES_KEY = stringPreferencesKey("intereses")
    val UBICACION_KEY = stringPreferencesKey("ubicacion")
    val ONBOARDING_KEY = stringPreferencesKey("onboarding_shown")
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    val PREMIUM_USER_KEY = booleanPreferencesKey("premium_user")
    val USER_LOGGED_IN_KEY = booleanPreferencesKey("user_logged_in")

    suspend fun saveAIType(context: Context, type: CommentAIType) {
        context.dataStore.edit { prefs ->
            prefs[AI_TYPE_KEY] = type.name
        }
    }

    fun getAIType(context: Context): Flow<CommentAIType?> =
        context.dataStore.data.map { prefs ->
            prefs[AI_TYPE_KEY]?.let { CommentAIType.valueOf(it) }
        }

    suspend fun saveUserProfile(context: Context, profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[NOMBRE_KEY] = profile.nombre
            prefs[APELLIDOS_KEY] = profile.apellidos
            prefs[USUARIO_KEY] = profile.usuario
            prefs[FOTO_KEY] = profile.fotoPerfilUri ?: ""
            prefs[BIOGRAFIA_KEY] = profile.biografia
            prefs[INTERESES_KEY] = profile.intereses
            prefs[UBICACION_KEY] = profile.ubicacion
            prefs[PREMIUM_USER_KEY] = profile.isPremium
        }
    }

    fun getUserProfile(context: Context): Flow<UserProfile> =
        context.dataStore.data.map { prefs ->
            UserProfile(
                nombre = prefs[NOMBRE_KEY] ?: "",
                apellidos = prefs[APELLIDOS_KEY] ?: "",
                usuario = prefs[USUARIO_KEY] ?: "",
                fotoPerfilUri = prefs[FOTO_KEY],
                biografia = prefs[BIOGRAFIA_KEY] ?: "",
                intereses = prefs[INTERESES_KEY] ?: "",
                ubicacion = prefs[UBICACION_KEY] ?: "",
                isPremium = prefs[PREMIUM_USER_KEY] ?: false
            )
        }

    suspend fun setOnboardingShown(context: Context, shown: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_KEY] = shown.toString()
        }
    }

    fun isOnboardingShown(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_KEY]?.toBoolean() ?: false
        }
        
    suspend fun setDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = isDarkMode
        }
    }
    
    fun getDarkMode(context: Context): Flow<Boolean?> =
        context.dataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY]
        }
        
    suspend fun setPremiumUser(context: Context, isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PREMIUM_USER_KEY] = isPremium
        }
    }
    
    fun isPremiumUser(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PREMIUM_USER_KEY] ?: false
        }

    suspend fun setUserLoggedIn(context: Context, isLoggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USER_LOGGED_IN_KEY] = isLoggedIn
        }
    }
    
    fun isUserLoggedIn(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[USER_LOGGED_IN_KEY] ?: false
        }
} 