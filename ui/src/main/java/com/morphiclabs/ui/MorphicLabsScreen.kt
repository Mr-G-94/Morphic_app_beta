package com.morphiclabs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morphiclabs.core.MessageProcessor
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import kotlinx.coroutines.launch

// This would typically be provided by DI
// For now, let's create a dummy implementation or expect it as a parameter
class DummyMessageProcessor : MessageProcessor {
    override suspend fun procesarMensaje(texto: String): String {
        return "$texto procesado (Dummy)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphicLabsScreen(
    modifier: Modifier = Modifier,
    messageProcessor: MessageProcessor = DummyMessageProcessor() // Default for preview/testing
) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Esperando entrada...") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
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

@Preview(showBackground = true)
@Composable
fun MorphicLabsScreenPreview() {
    MorphicLabsAppTheme {
        MorphicLabsScreen()
    }
}
