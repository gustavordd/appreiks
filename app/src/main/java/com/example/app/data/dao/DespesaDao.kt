package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.entities.Despesa

@Dao
interface DespesaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(despesa: Despesa): Long

    @Delete
    suspend fun deletar(despesa: Despesa)

    @Update
    suspend fun atualizar(despesa: Despesa)


    @Query("SELECT * FROM despesas WHERE propriedadeId = :propriedadeId")
    suspend fun listarPorPropriedade(propriedadeId: Long): List<Despesa>

    @Query("SELECT SUM(valor) FROM despesas WHERE propriedadeId = :propriedadeId")
    suspend fun totalGastos(propriedadeId: Long): Double?

    @Query("SELECT SUM(valor) FROM despesas WHERE propriedadeId = :propriedadeId")
    suspend fun getTotalDespesas(propriedadeId: Long): Double?

    @Query("SELECT * FROM despesas WHERE propriedadeId = :propriedadeId ORDER BY data DESC")
    suspend fun getDespesasDaPropriedade(propriedadeId: Long): List<Despesa>

}
