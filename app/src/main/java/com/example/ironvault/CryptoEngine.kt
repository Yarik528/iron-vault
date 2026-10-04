package com.example.ironvault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class CryptoEngine {

    companion object {
        private const val KEYSTORE_ALIAS = "iron_vault_master_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PBKDF2_ITERATIONS = 600_000 // Жёсткая защита от перебора
        private const val SALT_LENGTH = 32
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
    }

    private val secureRandom = SecureRandom()

    // Генерация мастер-ключа в аппаратном Keystore (неизвлекаемый)
    fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    // Вывод ключа из пароля через PBKDF2
    fun deriveKeyFromPassword(password: String, salt: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        return factory.generateSecret(spec).encoded
    }

    // Шифрование данных
    fun encrypt(data: String, password: String): EncryptedData {
        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { secureRandom.nextBytes(it) }
        
        val derivedKey = deriveKeyFromPassword(password, salt)
        val masterKey = getOrCreateMasterKey()
        
        // Комбинируем парольный ключ и мастер-ключ из Keystore
        val combinedKey = combineKeys(derivedKey, masterKey.encoded)
        val secretKey = SecretKeySpec(combinedKey, "AES")
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH, iv))
        
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return EncryptedData(
            ciphertext = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    // Расшифровка данных
    fun decrypt(encryptedData: EncryptedData, password: String): String {
        val salt = Base64.decode(encryptedData.salt, Base64.NO_WRAP)
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val ciphertext = Base64.decode(encryptedData.ciphertext, Base64.NO_WRAP)
        
        val derivedKey = deriveKeyFromPassword(password, salt)
        val masterKey = getOrCreateMasterKey()
        
        val combinedKey = combineKeys(derivedKey, masterKey.encoded)
        val secretKey = SecretKeySpec(combinedKey, "AES")
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH, iv))
        
        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    // XOR комбинация двух ключей
    private fun combineKeys(key1: ByteArray, key2: ByteArray): ByteArray {
        val result = ByteArray(key1.size)
        for (i in key1.indices) {
            result[i] = (key1[i].toInt() xor key2[i % key2.size].toInt()).toByte()
        }
        return result
    }

    // Генерация случайного пароля
    fun generatePassword(length: Int = 20): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
        return (1..length).map { chars[secureRandom.nextInt(chars.length)] }.joinToString("")
    }
}

data class EncryptedData(
    val ciphertext: String,
    val salt: String,
    val iv: String
)
