package com.example.aibomm.data

import android.content.Context

class DatabaseProvider private constructor(private val context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }

    val todoRepository: TodoRepository by lazy { TodoRepository(database.todoDao()) }

    companion object {
        @Volatile
        private var instance: DatabaseProvider? = null

        fun getInstance(context: Context): DatabaseProvider {
            return instance ?: synchronized(this) {
                DatabaseProvider(context.applicationContext).also { instance = it }
            }
        }
    }
}
