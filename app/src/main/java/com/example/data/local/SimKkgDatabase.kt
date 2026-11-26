package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AgendaEntity
import com.example.data.model.GalleryItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        TransactionEntity::class,
        AgendaEntity::class,
        GalleryItemEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SimKkgDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun agendaDao(): AgendaDao
    abstract fun galleryDao(): GalleryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SimKkgDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `username` TEXT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `schoolName` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`username`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): SimKkgDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SimKkgDatabase::class.java,
                    "sim_kkg_pai.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
