package com.nuecho.genesys.cli.preferences

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

class PasswordTest : StringSpec() {

    private val encryptedPassword = "9ndcYhfWmeljq9P5aY7dUcbIIIpvS/VBWFiKq0g7mKwCOJPm+/yJC67BAOWyK8Sk"
    private val clearPassword = "password"

    init {
        "calling isEncrypted should say if the string is encrypted like we do" {

            Password.isEncrypted("password") shouldBe false
            Password.isEncrypted("niuavlhefa78in28908yrhlsdal;") shouldBe false

            Password.isEncrypted(encryptedPassword) shouldBe true
            Password.isEncrypted("W544hT5DtdEbsxN565b9mGGsjVK7Sb12GuYJ8fqE04e8uHl/WL5HPzAagqOqJ8db") shouldBe true
        }

        "calling decrypt should return clear text" {
            Password.decrypt(encryptedPassword) shouldBe clearPassword
        }

        "calling encryptAndDigest should return encrypted text with digest" {
            Password.encryptAndDigest(clearPassword) shouldBe encryptedPassword
        }

        "calling promptPassword with no console attached should throw" {
            shouldThrow<IllegalStateException> {
                Password.promptForPassword()
            }
        }
    }
}
