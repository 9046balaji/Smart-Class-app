package com.vfstr.smartclass.utils.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor() {

    private val KEY_ALIAS = "com.vfstr.smartclass.biometric_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"

    init {
        initSecureKey()
    }

    private fun initSecureKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                // CRITICAL: Forces biometric confirmation to release this key
                .setUserAuthenticationRequired(true)
                // CRITICAL: If a user adds a new fingerprint to the phone, invalidate the key instantly
                .setInvalidatedByBiometricEnrollment(true)
                .build()
            )
            keyGenerator.generateKey()
        }
    }

    fun getInitializedCipher(opMode: Int, iv: ByteArray? = null): Cipher {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        if (opMode == Cipher.ENCRYPT_MODE) {
            cipher.init(opMode, key)
        } else {
            if (iv == null) throw IllegalArgumentException("IV is required for decryption")
            cipher.init(opMode, key, IvParameterSpec(iv))
        }
        return cipher
    }
}
