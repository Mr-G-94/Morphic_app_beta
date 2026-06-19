package com.morphiclabs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morphiclabs.core.MessageProcessor
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import kotlinx.coroutines.launch

class DummyMessageProcessor : MessageProcessor {
    override suspend fun procesarMensaje(texto: String): String {
        return "$texto procesado (Dummy)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphicLabsScreen(
    modifier: Modifier = Modifier,
    messageProcessor: MessageProcessor = DummyMessageProcessor(),
    onNavigateToSettings: () -> Unit 
) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Esperando entrada...") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Morphic Labs") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Introduce un mensaje") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        resultText = "Procesando..."
                        resultText = messageProcessor.procesarMensaje(inputText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Procesar")
            }
            Text(text = "Resultado: $resultText")
        }
    }
}
