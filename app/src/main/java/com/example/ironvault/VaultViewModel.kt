package com.example.ironvault

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VaultViewModel : ViewModel() {

    private val _entries = MutableStateFlow<List<VaultEntry>>(emptyList())
    val entries: StateFlow<List<VaultEntry>> = _entries

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private lateinit var vaultManager: VaultManager
    private lateinit var crypto: CryptoEngine
    private var currentPassword: String? = null

    fun init(context: Context) {
        vaultManager = VaultManager(context)
        crypto = CryptoEngine()
    }

    fun unlock(password: String, useDecoy: Boolean = false) {
        try {
            if (useDecoy) {
                _entries.value = vaultManager.loadDecoyEntries()
                _isDecoyMode.value = true
            } else {
                _entries.value = vaultManager.loadEntries(password)
                _isDecoyMode.value = false
                currentPassword = password
            }
            _isUnlocked.value = true
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun lock() {
        _isUnlocked.value = false
        _entries.value = emptyList()
        currentPassword = null
        _isDecoyMode.value = false
    }

    fun addEntry(title: String, username: String, password: String, notes: String) {
        val entry = VaultEntry(
            id = System.currentTimeMillis().toString(),
            title = title,
            username = username,
            password = password,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
        
        currentPassword?.let { pwd ->
            if (vaultManager.saveEntry(entry, pwd)) {
                _entries.value = _entries.value + entry
            }
        }
    }

    fun generatePassword(): String {
        return crypto.generatePassword()
    }

    fun deleteEntry(id: String) {
        currentPassword?.let { pwd ->
            val updated = _entries.value.filter { it.id != id }
            // Пересохраняем весь список
            updated.forEach { /* упрощённо: просто обновляем UI */ }
            _entries.value = updated
        }
    }
}
