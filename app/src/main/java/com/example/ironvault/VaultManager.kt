package com.example.ironvault

import android.content.Context
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
    
    // Зашифрованное хранилище через Android Keystore
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "iron_vault_data",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var failedAttempts = 0
    private val MAX_ATTEMPTS = 10

    // Сохранение записи
    fun saveEntry(entry: VaultEntry, password: String): Boolean {
        return try {
            val entries = loadEntries(password).toMutableList()
            entries.add(entry)
            val json = gson.toJson(entries)
            val encrypted = crypto.encrypt(json, password)
            
            prefs.edit()
                .putString("vault_data", gson.toJson(encrypted))
                .apply()
            
            failedAttempts = 0
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Загрузка записей
    fun loadEntries(password: String): List<VaultEntry> {
        return try {
            val encryptedJson = prefs.getString("vault_data", null)
                ?: return emptyList()
            
            val encrypted = gson.fromJson(encryptedJson, EncryptedData::class.java)
            val decryptedJson = crypto.decrypt(encrypted, password)
            
            val type = object : TypeToken<List<VaultEntry>>() {}.type
            failedAttempts = 0
            gson.fromJson(decryptedJson, type) ?: emptyList()
        } catch (e: Exception) {
            failedAttempts++
            if (failedAttempts >= MAX_ATTEMPTS) {
                wipeAllData()
                throw SecurityException("СЛИШКОМ МНОГО ПОПЫТОК. ДАННЫЕ УНИЧТОЖЕНЫ.")
            }
            throw SecurityException("Неверный пароль. Попыток осталось: ${MAX_ATTEMPTS - failedAttempts}")
        }
    }

    // Автоуничтожение
    private fun wipeAllData() {
        prefs.edit().clear().apply()
    }

    // Проверка, есть ли данные
    fun hasData(): Boolean {
        return prefs.contains("vault_data")
    }

    // Фейковый режим (пустой сейф)
    fun loadDecoyEntries(): List<VaultEntry> {
        return listOf(
            VaultEntry("1", "Google", "user@gmail.com", "password123", "Фейковая запись", System.currentTimeMillis()),
            VaultEntry("2", "Instagram", "my_account", "qwerty", "Ничего важного", System.currentTimeMillis())
        )
    }
}
