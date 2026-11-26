package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AgendaEntity
import com.example.data.model.GalleryItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionEntity>)

    @Update
    suspend fun update(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agendas ORDER BY CASE WHEN status = 'UPCOMING' THEN 0 ELSE 1 END, dateTime ASC")
    fun observeAll(): Flow<List<AgendaEntity>>

    @Query("SELECT * FROM agendas ORDER BY dateTime ASC")
    suspend fun getAllOnce(): List<AgendaEntity>

    @Query("SELECT COUNT(*) FROM agendas")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AgendaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaEntity>)

    @Update
    suspend fun update(item: AgendaEntity)

    @Query("UPDATE agendas SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Query("DELETE FROM agendas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM agendas")
    suspend fun deleteAll()
}

@Dao
interface GalleryDao {
    @Query("SELECT * FROM gallery_items ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<GalleryItemEntity>>

    @Query("SELECT * FROM gallery_items ORDER BY date DESC, createdAt DESC")
    suspend fun getAllOnce(): List<GalleryItemEntity>

    @Query("SELECT COUNT(*) FROM gallery_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GalleryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GalleryItemEntity>)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM gallery_items")
    suspend fun deleteAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE role != 'ADMIN' ORDER BY CASE WHEN status = 'PENDING' THEN 0 WHEN status = 'VERIFIED' THEN 1 ELSE 2 END, createdAt DESC")
    fun observeMembers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role != 'ADMIN' AND status = 'PENDING'")
    fun observePendingMemberCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE role != 'ADMIN' ORDER BY createdAt DESC")
    suspend fun getMembersOnce(): List<UserEntity>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("UPDATE users SET status = :status WHERE username = :username AND role != 'ADMIN'")
    suspend fun updateMemberStatus(username: String, status: String)

    @Query("DELETE FROM users WHERE username = :username AND role != 'ADMIN'")
    suspend fun deleteMember(username: String)

    @Query("DELETE FROM users WHERE role != 'ADMIN'")
    suspend fun deleteAllMembers()
}
