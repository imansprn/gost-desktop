package xyz.gobliggg.gost.data

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EncryptionUtilTest {
    @Test
    fun `test encryption and decryption roundtrip`() {
        val original = "secret-password-123"
        val encrypted = EncryptionUtil.encrypt(original)

        assertNotEquals(original, encrypted)

        val decrypted = EncryptionUtil.decrypt(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun `test encryption is randomized`() {
        val original = "same-password"
        val enc1 = EncryptionUtil.encrypt(original)
        val enc2 = EncryptionUtil.encrypt(original)

        // Due to random salt and IV, two encryptions of the same text should be different
        assertNotEquals(enc1, enc2)

        // But both should decrypt to the same text
        assertEquals(original, EncryptionUtil.decrypt(enc1))
        assertEquals(original, EncryptionUtil.decrypt(enc2))
    }
}
