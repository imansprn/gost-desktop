package xyz.gobliggg.gost.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-CBC encryption utility for storing passwords locally.
 * Uses a machine-derived key so passwords are not stored in plaintext.
 */
object EncryptionUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 65536
    private const val IV_LENGTH = 16
    private const val SALT_LENGTH = 16

    // Derive a stable passphrase from machine properties
    private val masterPassphrase: CharArray by lazy {
        val props = listOf(
            System.getProperty("user.name", "gost"),
            System.getProperty("os.name", "desktop"),
            System.getProperty("user.home", "/tmp"),
        )
        "gost-manager-${props.joinToString("-")}".toCharArray()
    }

    fun encrypt(plainText: String): String {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Encode as: base64(salt + iv + ciphertext)
        val combined = salt + iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encoded: String): String {
        val combined = Base64.getDecoder().decode(encoded)

        val salt = combined.sliceArray(0 until SALT_LENGTH)
        val iv = combined.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
        val ciphertext = combined.sliceArray(SALT_LENGTH + IV_LENGTH until combined.size)

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }
}
