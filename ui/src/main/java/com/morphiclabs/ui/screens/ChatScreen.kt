package com.morphiclabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morphiclabs.core.MessageProcessor
import com.morphiclabs.ui.components.MorphicCard
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(messageProcessor: MessageProcessor) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Esperando entrada...") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("AGENT CHAT", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        MorphicCard {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Mensaje al Agente") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            resultText = "Procesando..."
                            resultText = messageProcessor.procesarMensaje(inputText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enviar")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        MorphicCard {
            Text(text = resultText, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
