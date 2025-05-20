package com.example.venusai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.venusai.data.CommentAIType

@Composable
fun SelectAITypeScreen(
    onTypeSelected: (CommentAIType) -> Unit
) {
    val aiTypes = CommentAIType.values()
    var selectedType by remember { mutableStateOf<CommentAIType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¿Qué tipo de IA quieres para los comentarios?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        aiTypes.forEach { type ->
            Button(
                onClick = {
                    selectedType = type
                    onTypeSelected(type)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(type.displayName)
            }
        }
    }
} 