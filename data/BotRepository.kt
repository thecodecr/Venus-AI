package com.example.venusai.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bots_prefs")

object BotRepository {
    private val BOTS_KEY = stringPreferencesKey("bots")
    private val gson = Gson()

    suspend fun saveBots(context: Context, bots: List<BotFollower>) {
        val json = gson.toJson(bots)
        context.dataStore.edit { it[BOTS_KEY] = json }
    }

    fun getBots(context: Context): Flow<List<BotFollower>> =
        context.dataStore.data.map {
            val json = it[BOTS_KEY] ?: "[]"
            gson.fromJson(json, object : TypeToken<List<BotFollower>>() {}.type)
        }
} 