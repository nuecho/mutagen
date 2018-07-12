package com.nuecho.genesys.cli.preferences

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Passwords {
    private const val KEY = "***REMOVED***"

    private const val KEY_TYPE = "AES"
    private const val CIPHER = "AES/ECB/PKCS5Padding"
    private const val DIGEST = "SHA-256"
    private const val DIGEST_BYTES_SIZE = 32

    private val key: SecretKey

    init {
        val decodedKey = Base64.getDecoder().decode(KEY)
        key = SecretKeySpec(decodedKey, 0, decodedKey.size, KEY_TYPE)
    }

    fun encryptAndDigest(input: String): String {
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val encryptedBytes = encrypt(inputBytes)
        val digest = digest(encryptedBytes)

        val token = encryptedBytes + digest

        return token.toBase64String()
    }

    fun decrypt(input: String): String {
        val inputBytes = input.base64ToByteArray()
        val decryptedBytes = decrypt(inputBytes.getPayload())
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun isEncrypted(input: String?): Boolean {
        if (input == null) return false
        try {
            val inputBytes = input.base64ToByteArray()
            if (inputBytes.size <= DIGEST_BYTES_SIZE) return false

            val actualDigest = digest(inputBytes.getPayload())
            return actualDigest.contentEquals(inputBytes.getDigest())
        } catch (exception: Exception) {
            return false
        }
    }

    private fun encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(input)
    }

    private fun decrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(input)
    }

    private fun digest(input: ByteArray): ByteArray = MessageDigest.getInstance(DIGEST).digest(input)

    private fun ByteArray.toBase64String(): String = Base64.getEncoder().encodeToString(this)
    private fun String.base64ToByteArray(): ByteArray = Base64.getDecoder().decode(this)

    private fun ByteArray.getDigest(): ByteArray = sliceArray(this.size - DIGEST_BYTES_SIZE until this.size)
    private fun ByteArray.getPayload(): ByteArray = sliceArray(0 until this.size - DIGEST_BYTES_SIZE)
}
