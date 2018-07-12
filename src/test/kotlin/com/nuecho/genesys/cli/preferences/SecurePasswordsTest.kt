package com.nuecho.genesys.cli.preferences

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

class SecurePasswordsTest {

    private val encryptedPassword = "9ndcYhfWmeljq9P5aY7dUcbIIIpvS/VBWFiKq0g7mKwCOJPm+/yJC67BAOWyK8Sk"
    private val clearPassword = "password"

    @Test
    fun `calling isEncrypted should say if the string is encrypted like we do`() {

        assertThat(Passwords.isEncrypted("password"), `is`(false))
        assertThat(Passwords.isEncrypted("niuavlhefa78in28908yrhlsdal;"), `is`(false))

        assertThat(Passwords.isEncrypted(encryptedPassword), `is`(true))
        assertThat(Passwords.isEncrypted("W544hT5DtdEbsxN565b9mGGsjVK7Sb12GuYJ8fqE04e8uHl/WL5HPzAagqOqJ8db"), `is`(true))
    }

    @Test
    fun `calling decrypt should return clear text`() {
        assertThat(Passwords.decrypt(encryptedPassword), equalTo(clearPassword))
    }

    @Test
    fun `calling encryptAndDigest should return encrypted text with digest`() {
        assertThat(Passwords.encryptAndDigest(clearPassword), equalTo(encryptedPassword))
    }

    @Test
    fun `password should clear its value after accessing it`() {
        val passwordString = "password"
        val password = SecurePassword(passwordString.toCharArray())
        assertThat(password.value, equalTo(passwordString))
        assertThat(password.value, not(equalTo(encryptedPassword)))
    }
}
