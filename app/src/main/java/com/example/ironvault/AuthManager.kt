package com.example.ironvault

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class AuthType {
    NONE, PIN, PASSWORD, PATTERN
}

class AuthManager(context: Context) {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "auth_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val crypto = CryptoEngine()
    
    fun isFirstLaunch(): Boolean = !prefs.contains("auth_type")
    
    fun getAuthType(): AuthType {
        val type = prefs.getString("auth_type", null) ?: return AuthType.NONE
        return try {
            AuthType.valueOf(type)
        } catch (e: Exception) {
            AuthType.NONE
        }
    }
    
    fun setAuthType(type: AuthType) {
        prefs.edit().putString("auth_type", type.name).apply()
    }
    
    fun setupPin(pin: String): Boolean {
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hashed = hashPin(pin, salt)
        
        prefs.edit()
            .putString("pin_hash", hashed)
            .putString("pin_salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString("auth_type", AuthType.PIN.name)
            .apply()
        return true
    }
    
    fun setupPassword(password: String): Boolean {
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hashed = hashPassword(password, salt)
        
        prefs.edit()
            .putString("password_hash", hashed)
            .putString("password_salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString("auth_type", AuthType.PASSWORD.name)
            .apply()
        return true
    }
    
    fun setupPattern(pattern: List<Int>): Boolean {
        val patternString = pattern.joinToString("-")
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hashed = hashPattern(patternString, salt)
        
        prefs.edit()
            .putString("pattern_hash", hashed)
            .putString("pattern_salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString("auth_type", AuthType.PATTERN.name)
            .apply()
        return true
    }
    
    fun verifyPin(pin: String): Boolean {
        val saltBase64 = prefs.getString("pin_salt", null) ?: return false
        val savedHash = prefs.getString("pin_hash", null) ?: return false
        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val computed = hashPin(pin, salt)
        return MessageDigest.isEqual(computed.toByteArray(), savedHash.toByteArray())
    }
    
    fun verifyPassword(password: String): Boolean {
        val saltBase64 = prefs.getString("password_salt", null) ?: return false
        val savedHash = prefs.getString("password_hash", null) ?: return false
        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val computed = hashPassword(password, salt)
        return MessageDigest.isEqual(computed.toByteArray(), savedHash.toByteArray())
    }
    
    fun verifyPattern(pattern: List<Int>): Boolean {
        val saltBase64 = prefs.getString("pattern_salt", null) ?: return false
        val savedHash = prefs.getString("pattern_hash", null) ?: return false
        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val patternString = pattern.joinToString("-")
        val computed = hashPattern(patternString, salt)
        return MessageDigest.isEqual(computed.toByteArray(), savedHash.toByteArray())
    }
    
    fun clearAuth() {
        prefs.edit().clear().apply()
    }
    
    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return android.util.Base64.encodeToString(factory.generateSecret(spec).encoded, android.util.Base64.NO_WRAP)
    }
    
    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 600000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return android.util.Base64.encodeToString(factory.generateSecret(spec).encoded, android.util.Base64.NO_WRAP)
    }
    
    private fun hashPattern(pattern: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pattern.toCharArray(), salt, 50000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return android.util.Base64.encodeToString(factory.generateSecret(spec).encoded, android.util.Base64.NO_WRAP)
    }
}
