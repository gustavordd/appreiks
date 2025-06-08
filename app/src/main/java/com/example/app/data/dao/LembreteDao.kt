package com.example.app.data.dao

import androidx.room.*
import com.example.app.entities.Lembrete

@Dao
interface LembreteDao {

    @Insert
    suspend fun inserir(lembrete: Lembrete)

    @Query("SELECT * FROM Lembrete WHERE propriedadeId = :propriedadeId")
    suspend fun getPorPropriedade(propriedadeId: Long): List<Lembrete>

    @Delete
    suspend fun deletar(lembrete: Lembrete)

    @Update
    suspend fun atualizar(lembrete: Lembrete)
}
