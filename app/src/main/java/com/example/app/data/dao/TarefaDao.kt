package com.example.app.data.dao

import androidx.room.*
import com.example.app.entities.Tarefa

@Dao
interface TarefaDao {

    @Insert
    suspend fun inserir(tarefa: Tarefa)

    @Update
    suspend fun atualizar(tarefa: Tarefa)

    @Delete
    suspend fun deletar(tarefa: Tarefa)

    @Query("SELECT * FROM Tarefa WHERE propriedadeId = :propriedadeId ORDER BY data, hora")
    suspend fun getTarefasPorPropriedade(propriedadeId: Long): List<Tarefa>
}


