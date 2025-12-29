package com.example.aibomm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val refinedText: String? = null,
    val summary: String? = null,
    val tags: String = "", // Comma separated tags
    val isDone: Boolean = false,
    val isAiProcessed: Boolean = false,
    val category: String = "text", // text, audio, link, task
    val intent: String? = null, // calendar, alarm, task
    val intentPayload: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
