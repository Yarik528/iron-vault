package com.example.ironvault

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val createdAt: Long
)

class VaultManager(private val context: Context) {

    private val crypto = CryptoEngine()
    private val gson = Gson()

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "iron_vault_data",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getVaultKey(): String {
        var key = prefs.getString("vault_key", null)
        if (key == null) {
            key = java.util.UUID.randomUUID().toString() +
                    java.util.UUID.randomUUID().toString()
            prefs.edit().putString("vault_key", key).apply()
        }
        return key
    }

    fun saveEntry(entry: VaultEntry, password: String): Boolean {
        return try {
            val entries = loadEntries(password).toMutableList()
            entries.add(entry)
            saveEntriesDirect(entries, password)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveEntriesDirect(entries: List<VaultEntry>, password: String): Boolean {
        return try {
            val json = gson.toJson(entries)
            val encrypted = crypto.encrypt(json, password)
            prefs.edit().putString("vault_data", gson.toJson(encrypted)).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadEntries(password: String): List<VaultEntry> {
        return try {
            val encryptedJson = prefs.getString("vault_data", null) ?: return emptyList()
            val encrypted = gson.fromJson(encryptedJson, EncryptedData::class.java)
            val decryptedJson = crypto.decrypt(encrypted, password)
            val type = object : TypeToken<List<VaultEntry>>() {}.type
            gson.fromJson(decryptedJson, type) ?: emptyList()
        } catch (e: Exception) {
            throw SecurityException("Неверный пароль сейфа")
        }
    }

    fun wipeAllData() {
        prefs.edit().clear().apply()
    }

    fun hasData(): Boolean = prefs.contains("vault_data")

    fun loadDecoyEntries(): List<VaultEntry> {
        return listOf(
            VaultEntry("1", "Google", "user@gmail.com", "password123", "Фейковая запись", System.currentTimeMillis()),
            VaultEntry("2", "Instagram", "my_account", "qwerty", "Ничего важного", System.currentTimeMillis())
        )
    }
}
