package com.example.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tarefa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propriedadeId: Long,
    val titulo: String,
    val data: String,
    val hora: String,
    val observacoes: String,
    val concluida: Boolean = false
)
