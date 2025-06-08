package com.example.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "propriedades")
data class Propriedade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val administradorId: Long,
    val nome: String,
    val endereco: String,
    val descricao: String,
    val fotoUri: String? = null,
    var caminhoContrato: String? = null,
    val inquilinoId: Long? = null
)
