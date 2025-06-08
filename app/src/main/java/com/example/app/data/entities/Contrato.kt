package com.example.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contratos")
data class Contrato(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propriedadeId: Long,
    val arquivoUri: String
)
