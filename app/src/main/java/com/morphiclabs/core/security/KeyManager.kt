package com.morphiclabs.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class KeyManager {
    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            "secure_api_keys",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveApiKey(context: Context, provider: String, key: String) {
        getEncryptedPrefs(context).edit().putString(provider, key).apply()
    }

    fun getApiKey(context: Context, provider: String): String? {
        return getEncryptedPrefs(context).getString(provider, null)
    }
}
