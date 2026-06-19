package com.morphiclabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.morphiclabs.core.security.AppConfigManager
import com.morphiclabs.core.base.ModelProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    configManager: AppConfigManager,
    modelProvider: ModelProvider,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var telegramKey by remember { mutableStateOf(configManager.getApiKey("telegram") ?: "") }
    var whatsappKey by remember { mutableStateOf(configManager.getApiKey("whatsapp") ?: "") }
    var geminiKey by remember { mutableStateOf(configManager.getApiKey("gemini") ?: "") }

    var selectedModel by remember { mutableStateOf(configManager.getModel()) }
    var activeAgent by remember { mutableStateOf(configManager.getActiveAgent()) }
    var availableModels by remember { mutableStateOf(listOf<String>()) }
    var expanded by remember { mutableStateOf(false) }
    var agentExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Configuración BYOK", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = telegramKey, onValueChange = { telegramKey = it }, label = { Text("Telegram API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = whatsappKey, onValueChange = { whatsappKey = it }, label = { Text("WhatsApp API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = geminiKey, onValueChange = { geminiKey = it }, label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Text("Agente Activo", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = agentExpanded, onExpandedChange = { agentExpanded = !agentExpanded }) {
            OutlinedTextField(
                value = activeAgent, onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agentExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = agentExpanded, onDismissRequest = { agentExpanded = false }) {
                listOf("General", "Ventas", "Código").forEach { agent ->
                    DropdownMenuItem(text = { Text(agent) }, onClick = { activeAgent = agent; agentExpanded = false })
                }
            }
        }

        Button(onClick = {
            coroutineScope.launch {
                isLoading = true
                availableModels = modelProvider.fetchAvailableModels(geminiKey)
                isLoading = false
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLoading) "Cargando..." else "Cargar modelos disponibles")
        }

        if (availableModels.isNotEmpty()) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = selectedModel, onValueChange = {}, readOnly = true, label = { Text("Modelo Seleccionado") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableModels.forEach { model -> DropdownMenuItem(text = { Text(model) }, onClick = { selectedModel = model; expanded = false }) }
                }
            }
        }

        Button(onClick = {
            configManager.saveApiKey("telegram", telegramKey)
            configManager.saveApiKey("whatsapp", whatsappKey)
            configManager.saveApiKey("gemini", geminiKey)
            configManager.saveModel(selectedModel)
            configManager.saveActiveAgent(activeAgent)
            onSave()
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar y Activar") }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Regresar") }
    }
}
