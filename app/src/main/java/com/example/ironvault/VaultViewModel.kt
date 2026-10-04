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
    private var currentPassword: String? = null

    fun init(context: Context) {
        vaultManager = VaultManager(context)
        crypto = CryptoEngine()
        authManager = AuthManager(context)
        
        _isFirstLaunch.value = authManager.isFirstLaunch()
        _currentAuthType.value = authManager.getAuthType()
    }

    fun setupPin(pin: String): Boolean {
        val success = authManager.setupPin(pin)
        if (success) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PIN
        }
        return success
    }
    
    fun setupPassword(password: String): Boolean {
        val success = authManager.setupPassword(password)
        if (success) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PASSWORD
            currentPassword = password
        }
        return success
    }
    
    fun setupPattern(pattern: List<Int>): Boolean {
        val success = authManager.setupPattern(pattern)
        if (success) {
            _isFirstLaunch.value = false
            _currentAuthType.value = AuthType.PATTERN
        }
        return success
    }
    
    fun changeAuthType(type: AuthType) {
        authManager.setAuthType(type)
        _currentAuthType.value = type
    }

    fun unlockWithPin(pin: String, useDecoy: Boolean = false) {
        if (authManager.verifyPin(pin)) {
            unlockInternal(useDecoy)
        } else {
            _error.value = "Неверный PIN-код"
        }
    }
    
    fun unlockWithPassword(password: String, useDecoy: Boolean = false) {
        if (authManager.verifyPassword(password)) {
            currentPassword = password
            unlockInternal(useDecoy)
        } else {
            _error.value = "Неверный пароль"
        }
    }
    
    fun unlockWithPattern(pattern: List<Int>, useDecoy: Boolean = false) {
        if (authManager.verifyPattern(pattern)) {
            unlockInternal(useDecoy)
        } else {
            _error.value = "Неверный графический ключ"
        }
    }
    
    private fun unlockInternal(useDecoy: Boolean) {
        try {
            if (useDecoy) {
                _entries.value = vaultManager.loadDecoyEntries()
                _isDecoyMode.value = true
            } else {
                val password = currentPassword ?: ""
                _entries.value = vaultManager.loadEntries(password)
                _isDecoyMode.value = false
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
        val updated = _entries.value.filter { it.id != id }
        _entries.value = updated
    }
    
    fun getAuthManager(): AuthManager = authManager
}
