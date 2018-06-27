package com.nuecho.genesys.cli.preferences

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PasswordTest {

    private val encryptedPassword = "9ndcYhfWmeljq9P5aY7dUcbIIIpvS/VBWFiKq0g7mKwCOJPm+/yJC67BAOWyK8Sk"
    private val clearPassword = "password"

    @Test
    fun `calling isEncrypted should say if the string is encrypted like we do`() {

        assertThat(Password.isEncrypted("password"), `is`(false))
        assertThat(Password.isEncrypted("niuavlhefa78in28908yrhlsdal;"), `is`(false))

        assertThat(Password.isEncrypted(encryptedPassword), `is`(true))
        assertThat(Password.isEncrypted("W544hT5DtdEbsxN565b9mGGsjVK7Sb12GuYJ8fqE04e8uHl/WL5HPzAagqOqJ8db"), `is`(true))
    }

    @Test
    fun `calling decrypt should return clear text`() {
        assertThat(Password.decrypt(encryptedPassword), equalTo(clearPassword))
    }

    @Test
    fun `calling encryptAndDigest should return encrypted text with digest`() {
        assertThat(Password.encryptAndDigest(clearPassword), equalTo(encryptedPassword))
    }

    @Test
    fun `calling promptPassword with no console attached should throw`() {
        assertThrows(IllegalStateException::class.java) {
            Password.promptForPassword()
        }
    }
}
