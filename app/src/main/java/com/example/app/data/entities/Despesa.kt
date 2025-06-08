package com.example.app.data.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class Despesa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propriedadeId: Long,
    val valor: Double,
    val descricao: String,
    val data: String
)

