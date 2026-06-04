package com.morphiclabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.morphiclabs.core.security.AppConfigManager

@Composable
fun SettingsScreen(configManager: AppConfigManager, onBack: () -> Unit) {
    var telegramKey by remember { mutableStateOf(configManager.getApiKey("telegram") ?: "") }
    var whatsappKey by remember { mutableStateOf(configManager.getApiKey("whatsapp") ?: "") }
    var geminiKey by remember { mutableStateOf(configManager.getApiKey("gemini") ?: "") }
    var audioResponse by remember { mutableStateOf(configManager.getApiKey("mode_audio") == "true") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configuración BYOK", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = telegramKey,
            onValueChange = { telegramKey = it },
            label = { Text("Telegram API Token") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = whatsappKey,
            onValueChange = { whatsappKey = it },
            label = { Text("WhatsApp API Key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = geminiKey,
            onValueChange = { geminiKey = it },
            label = { Text("Gemini API Key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Preferir respuesta en Audio")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = audioResponse,
                onCheckedChange = { audioResponse = it }
            )
        }

        Button(
            onClick = {
                configManager.saveApiKey("telegram", telegramKey)
                configManager.saveApiKey("whatsapp", whatsappKey)
                configManager.saveApiKey("gemini", geminiKey)
                configManager.saveApiKey("mode_audio", audioResponse.toString())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Configuración")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al Chat")
        }
    }
}
