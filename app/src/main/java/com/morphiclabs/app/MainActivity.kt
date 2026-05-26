package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morphiclabs.app.ui.theme.MorphicLabsAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorphicLabsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MorphicLabsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphicLabsScreen(modifier: Modifier = Modifier) {
    var inputText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("Esperando mensaje...") }
    val middleware = remember { MiddlewareLocal() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Escribe tu mensaje") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    responseText = "Procesando..."
                    val result = middleware.procesarMensaje(inputText)
                    responseText = result
                }
            },
            enabled = inputText.isNotBlank()
        ) {
            Text("Enviar mensaje")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = responseText)
    }
}

@Preview(showBackground = true)
@Composable
fun MorphicLabsScreenPreview() {
    MorphicLabsAppTheme {
        MorphicLabsScreen()
    }
}
