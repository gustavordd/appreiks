package com.example.app.utils


import java.security.MessageDigest

object SecurityUtils {
    fun hashSenha(senha: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(senha.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
