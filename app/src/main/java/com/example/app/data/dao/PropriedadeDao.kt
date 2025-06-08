package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.entities.Propriedade

@Dao
interface PropriedadeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(propriedade: Propriedade): Long

    @Update
    suspend fun atualizar(propriedade: Propriedade)

    @Delete
    suspend fun deletar(propriedade: Propriedade)

    @Query("SELECT * FROM propriedades WHERE administradorId = :adminId")
    suspend fun listarPorAdministrador(adminId: Long): List<Propriedade>

    @Query("UPDATE propriedades SET caminhoContrato = :caminho WHERE id = :id")
    suspend fun atualizarCaminhoContrato(id: Long, caminho: String?)

    @Query("SELECT * FROM propriedades WHERE id = :id LIMIT 1")
    suspend fun getPropriedadePorId(id: Long): Propriedade?

    @Query("UPDATE propriedades SET inquilinoId = :inquilinoId WHERE id = :propriedadeId")
    suspend fun vincularInquilino(propriedadeId: Long, inquilinoId: Long)

    @Query("UPDATE propriedades SET inquilinoId = NULL WHERE id = :propriedadeId")
    suspend fun desvincularInquilino(propriedadeId: Long)

}
