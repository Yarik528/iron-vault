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

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch

    private val _currentAuthType = MutableStateFlow(AuthType.NONE)
    val currentAuthType: StateFlow<AuthType> = _currentAuthType

    private lateinit var vaultManager: VaultManager
    private lateinit var crypto: CryptoEngine
    private lateinit var authManager: AuthManager

    fun init(context: Context) {
        vaultManager = VaultManager(context)
        crypto = CryptoEngine()
        authManager = AuthManager(context)
        _isFirstLaunch.value = authManager.isFirstLaunch()
        _currentAuthType.value = authManager.getAuthType()
    }

    fun setupPin(pin: String): Boolean {
        val ok = authManager.setupPin(pin)
        if (ok) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PIN
        }
        return ok
    }

    fun setupPassword(password: String): Boolean {
        val ok = authManager.setupPassword(password)
        if (ok) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PASSWORD
        }
        return ok
    }

    fun setupPattern(pattern: List<Int>): Boolean {
        val ok = authManager.setupPattern(pattern)
        if (ok) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PATTERN
        }
        return ok
    }

    fun unlockWithPin(pin: String, useDecoy: Boolean = false) {
        if (authManager.verifyPin(pin)) onSuccess(useDecoy) else onFailure()
    }

    fun unlockWithPassword(password: String, useDecoy: Boolean = false) {
        if (authManager.verifyPassword(password)) onSuccess(useDecoy) else onFailure()
    }

    fun unlockWithPattern(pattern: List<Int>, useDecoy: Boolean = false) {
        if (authManager.verifyPattern(pattern)) onSuccess(useDecoy) else onFailure()
    }

    private fun onSuccess(useDecoy: Boolean) {
        authManager.resetFailedAttempts()
        try {
            if (useDecoy) {
                _entries.value = vaultManager.loadDecoyEntries()
                _isDecoyMode.value = true
            } else {
                _entries.value = vaultManager.loadEntries(vaultManager.getVaultKey())
                _isDecoyMode.value = false
            }
            _isUnlocked.value = true
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    private fun onFailure() {
        val attempts = authManager.registerFailedAttempt()
        if (attempts >= 10) {
            vaultManager.wipeAllData()
            authManager.clearAuth()
            _isFirstLaunch.value = true
            _currentAuthType.value = AuthType.NONE
            _entries.value = emptyList()
            _isUnlocked.value = false
            _error.value = "ПРЕВЫШЕН ЛИМИТ ПОПЫТОК. ВСЕ ДАННЫЕ УНИЧТОЖЕНЫ."
        } else {
            _error.value = "Неверный код. Осталось попыток: ${10 - attempts}"
        }
    }

    fun lock() {
        _isUnlocked.value = false
        _entries.value = emptyList()
        _isDecoyMode.value = false
        _error.value = null
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
        if (vaultManager.saveEntry(entry, vaultManager.getVaultKey())) {
            _entries.value = _entries.value + entry
        }
    }

    fun deleteEntry(id: String) {
        val updated = _entries.value.filter { it.id != id }
        vaultManager.saveEntriesDirect(updated, vaultManager.getVaultKey())
        _entries.value = updated
    }

    fun generatePassword(): String = crypto.generatePassword()
}
