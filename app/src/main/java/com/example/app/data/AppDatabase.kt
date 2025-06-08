package com.example.app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.app.data.dao.*
import com.example.app.data.entities.*
import com.example.app.entities.Lembrete
import com.example.app.entities.Tarefa

@Database(
    entities = [
        Administrador::class,
        Propriedade::class,
        Inquilino::class,
        Contrato::class,
        Pagamento::class,
        Despesa::class,
        Lembrete::class,
        Tarefa::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun administradorDao(): AdministradorDao
    abstract fun propriedadeDao(): PropriedadeDao
    abstract fun inquilinoDao(): InquilinoDao
    abstract fun contratoDao(): ContratoDao
    abstract fun pagamentoDao(): PagamentoDao
    abstract fun despesaDao(): DespesaDao
    abstract fun lembreteDao(): LembreteDao
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE administradores ADD COLUMN fotoUri TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE propriedades ADD COLUMN caminhoContrato TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Tarefa ADD COLUMN concluida INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reiks_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // ⬅ Incluído aqui
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
