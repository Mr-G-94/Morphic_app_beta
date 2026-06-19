package com.morphiclabs.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppConfigManager(private val context: Context) {

    // Instancia para API Keys (Segura/Encriptada)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Instancia para ajustes generales (No necesita encriptación)
    private val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Gestión de API Keys
    fun saveApiKey(keyName: String, value: String) {
        sharedPreferences.edit().putString(keyName, value).apply()
    }

    fun getApiKey(keyName: String): String? {
        return sharedPreferences.getString(keyName, null)
    }

    // Gestión del modelo seleccionado
    fun saveModel(model: String) {
        settingsPrefs.edit().putString("selected_model", model).apply()
    }

    fun getModel(): String {
        // Retorna el modelo guardado o el valor por defecto "gemini-1.5-flash"
        return settingsPrefs.getString("selected_model", "gemini-1.5-flash") ?: "gemini-1.5-flash"
    }
}
