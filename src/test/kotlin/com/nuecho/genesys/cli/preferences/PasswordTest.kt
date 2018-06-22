package com.nuecho.genesys.cli.preferences

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows

class PasswordTest {

    private val encryptedPassword = "9ndcYhfWmeljq9P5aY7dUcbIIIpvS/VBWFiKq0g7mKwCOJPm+/yJC67BAOWyK8Sk"
    private val clearPassword = "password"

    @Test
    fun `calling isEncrypted should say if the string is encrypted like we do`() {

        assertFalse(Password.isEncrypted("password"))
        assertFalse(Password.isEncrypted("niuavlhefa78in28908yrhlsdal;"))

        assertTrue(Password.isEncrypted(encryptedPassword))
        assertTrue(Password.isEncrypted("W544hT5DtdEbsxN565b9mGGsjVK7Sb12GuYJ8fqE04e8uHl/WL5HPzAagqOqJ8db"))
    }

    @Test
    fun `calling decrypt should return clear text`() {
        assertEquals(Password.decrypt(encryptedPassword), clearPassword)
    }

    @Test
    fun `calling encryptAndDigest should return encrypted text with digest`() {
        assertEquals(Password.encryptAndDigest(clearPassword), encryptedPassword)
    }

    @Test
    fun `calling promptPassword with no console attached should throw`() {
        assertThrows(IllegalStateException::class.java) {
            Password.promptForPassword()
        }
    }
}
