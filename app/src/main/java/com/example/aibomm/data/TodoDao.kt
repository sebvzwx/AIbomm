package com.example.aibomm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isDone ASC, updatedAt DESC")
    fun observeAll(): Flow<List<TodoItem>>

    @Query("SELECT title FROM todos ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestTitle(): String?

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Long): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem): Long

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)
}
