package com.example.app.data.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Pagamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propriedadeId: Long,
    val valor: Double,
    val data: String,
    val descricao: String
)
