package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.entities.Administrador

@Dao
interface AdministradorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(administrador: Administrador): Long

    @Update
    suspend fun atualizar(administrador: Administrador)

    @Delete
    suspend fun deletar(administrador: Administrador)

    @Query("SELECT * FROM administradores WHERE email = :email AND senha = :senhaHash")
    suspend fun login(email: String, senhaHash: String): Administrador?

    @Query("SELECT * FROM administradores WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Administrador?

    @Query("SELECT * FROM administradores WHERE cpf = :cpf OR email = :email LIMIT 1")
    suspend fun buscarPorCpfOuEmail(cpf: String, email: String): Administrador?

    @Query("DELETE FROM administradores WHERE id = :id")
    suspend fun deletarPorId(id: Long)

}
