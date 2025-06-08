package com.example.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Lembrete(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propriedadeId: Long,
    val repeticao: String,
    val mensagem: String,
    val dia: Int
)
