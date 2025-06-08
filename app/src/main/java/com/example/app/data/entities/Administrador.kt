package com.example.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "administradores")
data class Administrador(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var nome: String,
    val cpf: String,
    var telefone: String,
    val email: String,
    var senha: String,
    var fotoUri: String? = null
)
