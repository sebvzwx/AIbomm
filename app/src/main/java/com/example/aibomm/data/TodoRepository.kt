package com.example.aibomm.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val dao: TodoDao) {
    fun observeAll(): Flow<List<TodoItem>> = dao.observeAll()

    suspend fun getById(id: Long): TodoItem? = dao.getById(id)

    suspend fun add(
        title: String,
        content: String = "",
        category: String = "text",
        tags: String = "",
        isAiProcessed: Boolean = false,
        summary: String? = null,
        refinedText: String? = null,
        intent: String? = null,
        intentPayload: String? = null
    ): Long {
        val now = System.currentTimeMillis()
        return dao.insert(
            TodoItem(
                title = title,
                content = content,
                category = category,
                tags = tags,
                isAiProcessed = isAiProcessed,
                summary = summary,
                refinedText = refinedText,
                intent = intent,
                intentPayload = intentPayload,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun update(todo: TodoItem) {
        dao.update(todo.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggle(todo: TodoItem) {
        dao.update(todo.copy(isDone = !todo.isDone, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(todo: TodoItem) {
        dao.delete(todo)
    }
}
