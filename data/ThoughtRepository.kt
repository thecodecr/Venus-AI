package com.example.venusai.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

object ThoughtRepository {
    private val Context.dataStore by preferencesDataStore(name = "thoughts_prefs")
    private val THOUGHTS_KEY = stringPreferencesKey("thoughts")
    private val COMMENTS_KEY = stringPreferencesKey("comments")
    private val gson = Gson()

    suspend fun saveThoughts(context: Context, thoughts: List<Thought>) {
        val json = gson.toJson(thoughts)
        context.dataStore.edit { it[THOUGHTS_KEY] = json }
    }

    fun getThoughts(context: Context): Flow<List<Thought>> =
        context.dataStore.data.map {
            val json = it[THOUGHTS_KEY] ?: "[]"
            gson.fromJson(json, object : TypeToken<List<Thought>>() {}.type)
        }

    suspend fun saveComments(context: Context, comments: Map<String, List<BotComment>>) {
        val json = gson.toJson(comments)
        context.dataStore.edit { it[COMMENTS_KEY] = json }
    }

    fun getComments(context: Context): Flow<Map<String, List<BotComment>>> =
        context.dataStore.data.map {
            val json = it[COMMENTS_KEY] ?: "{}"
            gson.fromJson(json, object : TypeToken<Map<String, List<BotComment>>>() {}.type)
        }
        
    // Función para dar like a un pensamiento
    suspend fun likeThought(context: Context, thoughtId: String, userId: String): Boolean {
        val thoughts = getThoughts(context).first()
        val updatedThoughts = thoughts.map { thought ->
            if (thought.id == thoughtId) {
                // Si ya dio like, quitamos el like (toggle)
                if (thought.likes.contains(userId)) {
                    thought.likes.remove(userId)
                } else {
                    thought.likes.add(userId)
                }
                thought
            } else {
                thought
            }
        }
        saveThoughts(context, updatedThoughts)
        return true
    }
    
    // Función para dar likes automáticos (bots)
    suspend fun addBotLikes(context: Context, thoughtId: String, botIds: List<String>) {
        val thoughts = getThoughts(context).first()
        val updatedThoughts = thoughts.map { thought ->
            if (thought.id == thoughtId) {
                // Añadimos likes de bots aleatorios
                botIds.forEach { botId ->
                    if (!thought.likes.contains(botId)) {
                        thought.likes.add(botId)
                    }
                }
                thought
            } else {
                thought
            }
        }
        saveThoughts(context, updatedThoughts)
    }
    
    // Función para retuitear
    suspend fun retweetThought(context: Context, thoughtId: String, userId: String, texto: String): String? {
        val thoughts = getThoughts(context).first()
        val originalThought = thoughts.find { it.id == thoughtId } ?: return null
        
        // Verificar si ya retuiteó
        if (originalThought.retweets.contains(userId)) {
            // Quitar el retweet (toggle)
            val updatedThoughts = thoughts.map { thought ->
                if (thought.id == thoughtId) {
                    thought.retweets.remove(userId)
                    thought
                } else {
                    // También eliminar el retweet existente
                    if (thought.isRetweet && thought.originalThoughtId == thoughtId && 
                        thought.texto == originalThought.texto) {
                        // No incluir este retweet en la nueva lista
                        null
                    } else {
                        thought
                    }
                }
            }.filterNotNull()
            
            saveThoughts(context, updatedThoughts)
            return null
        } else {
            // Añadir el retweet
            val newRetweet = Thought(
                id = UUID.randomUUID().toString(),
                texto = originalThought.texto,
                timestamp = System.currentTimeMillis(),
                imageUri = originalThought.imageUri,
                isRetweet = true,
                originalThoughtId = thoughtId
            )
            
            // Actualizar el recuento de retweets en el original
            val updatedThoughts = thoughts.map { thought ->
                if (thought.id == thoughtId) {
                    thought.retweets.add(userId)
                    thought
                } else {
                    thought
                }
            } + newRetweet
            
            saveThoughts(context, updatedThoughts)
            return newRetweet.id
        }
    }
    
    // Sincronizar con Post Repository (para mantener los tweets en ambas pantallas)
    // Esta función ha sido comentada porque PostRepository no existe en el proyecto
    /*
    suspend fun syncWithPosts(context: Context, postRepository: PostRepository) {
        val thoughts = getThoughts(context).first()
        // Convertir Thoughts a Posts
        thoughts.forEach { thought ->
            postRepository.addPost(
                context, 
                thought.texto, 
                thought.imageUri,
                thought.likes, 
                thought.retweets, 
                thought.isRetweet, 
                thought.originalThoughtId
            )
        }
    }
    */

    // Función para dar like a un comentario
    suspend fun likeComment(context: Context, thoughtId: String, commentId: String, userId: String): Boolean {
        val allComments = getComments(context).first()
        val comments = allComments[thoughtId] ?: return false
        
        val updatedComments = comments.map { comment ->
            if (comment.id == commentId) {
                // Si ya dio like, quitamos el like (toggle)
                if (comment.likes.contains(userId)) {
                    comment.likes.remove(userId)
                } else {
                    comment.likes.add(userId)
                }
                comment
            } else {
                comment
            }
        }
        
        val updatedAllComments = allComments.toMutableMap()
        updatedAllComments[thoughtId] = updatedComments
        saveComments(context, updatedAllComments)
        return true
    }
    
    // Función para añadir una respuesta a un comentario
    suspend fun addReplyToComment(context: Context, thoughtId: String, commentId: String, reply: CommentReply): Boolean {
        val allComments = getComments(context).first()
        val comments = allComments[thoughtId] ?: return false
        
        val updatedComments = comments.map { comment ->
            if (comment.id == commentId) {
                comment.replies.add(reply)
                comment
            } else {
                comment
            }
        }
        
        val updatedAllComments = allComments.toMutableMap()
        updatedAllComments[thoughtId] = updatedComments
        saveComments(context, updatedAllComments)
        return true
    }

    // Función para editar un pensamiento
    suspend fun editThought(context: Context, thoughtId: String, newText: String): Boolean {
        val thoughts = getThoughts(context).first()
        val updatedThoughts = thoughts.map { thought ->
            if (thought.id == thoughtId) {
                // Verificar si han pasado menos de 5 minutos desde la publicación
                val canEdit = System.currentTimeMillis() - thought.timestamp < 5 * 60 * 1000 // 5 minutos
                if (canEdit) {
                    thought.copy(texto = newText)
                } else {
                    thought
                }
            } else {
                thought
            }
        }
        saveThoughts(context, updatedThoughts)
        return true
    }
    
    // Función para eliminar un pensamiento
    suspend fun deleteThought(context: Context, thoughtId: String): Boolean {
        val thoughts = getThoughts(context).first()
        val updatedThoughts = thoughts.filter { it.id != thoughtId }
        saveThoughts(context, updatedThoughts)
        
        // También eliminar los comentarios asociados
        val allComments = getComments(context).first().toMutableMap()
        allComments.remove(thoughtId)
        saveComments(context, allComments)
        
        return true
    }
} 