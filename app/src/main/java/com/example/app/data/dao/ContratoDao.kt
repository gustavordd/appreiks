package com.example.app.data.dao


import androidx.room.*
import com.example.app.data.entities.Contrato

@Dao
interface ContratoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(contrato: Contrato): Long

    @Query("SELECT * FROM contratos WHERE propriedadeId = :propriedadeId")
    suspend fun buscarPorPropriedade(propriedadeId: Long): List<Contrato>
}
