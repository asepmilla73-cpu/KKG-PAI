package com.example.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    private val secureRandom = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val digest = sha256(salt, password)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(digest, Base64.NO_WRAP)
        return "$saltB64:$hashB64"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        return try {
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
            val actualHash = sha256(salt, password)
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun sha256(salt: ByteArray, password: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(password.toByteArray(Charsets.UTF_8))
        return md.digest()
    }
}
