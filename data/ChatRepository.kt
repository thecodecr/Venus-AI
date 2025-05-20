package com.example.venusai.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chats_prefs")

object ChatRepository {
    private val CHATS_KEY = stringPreferencesKey("chats")
    private val gson = Gson()

    suspend fun saveChats(context: Context, chats: Map<String, List<ChatMessage>>) {
        val json = gson.toJson(chats)
        context.dataStore.edit { it[CHATS_KEY] = json }
    }

    fun getChats(context: Context): Flow<Map<String, List<ChatMessage>>> =
        context.dataStore.data.map {
            val json = it[CHATS_KEY] ?: "{}"
            val typeToken = object : TypeToken<Map<String, List<ChatMessage>>>() {}.type
            gson.fromJson(json, typeToken)
        }
} 