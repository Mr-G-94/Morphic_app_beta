package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morphiclabs.core.MessageProcessor
import com.morphiclabs.data.MiddlewareLocal // Using direct instantiation for now, DI module will handle this
import com.morphiclabs.ui.MorphicLabsScreen
import com.morphiclabs.ui.theme.MorphicLabsAppTheme

class MainActivity : ComponentActivity() {
    // In a real DI setup, this would be injected by the :di module.
    // For now, we instantiate it directly to ensure the app compiles and runs.
    private val messageProcessor: MessageProcessor = MiddlewareLocal()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorphicLabsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MorphicLabsAppEntry(messageProcessor = messageProcessor)
                }
            }
        }
    }
}

@Composable
fun MorphicLabsAppEntry(messageProcessor: MessageProcessor) {
    MorphicLabsScreen(messageProcessor = messageProcessor)
}
