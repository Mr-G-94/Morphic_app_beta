package com.morphiclabs.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppConfigManager(private val context: Context) {
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

    private val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun saveApiKey(keyName: String, value: String) {
        sharedPreferences.edit().putString(keyName, value).apply()
    }

    fun getApiKey(keyName: String): String? {
        return sharedPreferences.getString(keyName, null)
    }

    fun saveModel(model: String) {
        settingsPrefs.edit().putString("selected_model", model).apply()
    }

    fun getModel(): String {
        return settingsPrefs.getString("selected_model", "gemini-1.5-flash") ?: "gemini-1.5-flash"
    }

    fun saveActiveAgent(agentName: String) {
        settingsPrefs.edit().putString("active_agent", agentName).apply()
    }

    fun getActiveAgent(): String {
        return settingsPrefs.getString("active_agent", "General") ?: "General"
    }
}
