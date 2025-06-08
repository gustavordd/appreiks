package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.entities.Inquilino

@Dao
interface InquilinoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(inquilino: Inquilino): Long

    @Update
    suspend fun atualizar(inquilino: Inquilino)

    @Delete
    suspend fun deletar(inquilino: Inquilino)

    @Query("SELECT * FROM inquilinos WHERE administradorId = :adminId")
    suspend fun listarPorAdministrador(adminId: Long): List<Inquilino>

    @Query("SELECT * FROM inquilinos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Inquilino?



}
