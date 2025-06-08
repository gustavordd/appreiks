package com.example.app.data.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inquilinos")
data class Inquilino(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val administradorId: Long,
    val nome: String,
    val email: String,
    val cpf: String,
    val contato: String,
    val contatoEmergencia: String,
    val fotoUri: String? = null,
    val propriedadeId: Long? = null
)