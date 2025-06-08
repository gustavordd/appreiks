package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.entities.Pagamento

@Dao
interface PagamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(pagamento: Pagamento): Long

    @Query("SELECT * FROM Pagamento WHERE propriedadeId = :propriedadeId")
    suspend fun listarPorPropriedade(propriedadeId: Long): List<Pagamento>

    @Query("SELECT SUM(valor) FROM Pagamento WHERE propriedadeId = :propriedadeId")
    suspend fun getTotalPagamentos(propriedadeId: Long): Double?

    @Query("SELECT * FROM Pagamento WHERE propriedadeId = :propriedadeId ORDER BY data DESC")
    suspend fun getPagamentosDaPropriedade(propriedadeId: Long): List<Pagamento>

    @Update
    suspend fun atualizar(pagamento: Pagamento)

    @Delete
    suspend fun deletar(pagamento: Pagamento)

}
