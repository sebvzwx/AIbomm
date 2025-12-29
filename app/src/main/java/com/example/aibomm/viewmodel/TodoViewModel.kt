package com.example.aibomm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aibomm.data.DatabaseProvider
import com.example.aibomm.data.TodoItem
import com.example.aibomm.data.TodoRepository
import com.example.aibomm.widget.TodoWidget1x1Provider
import com.example.aibomm.widget.TodoWidget2x2Provider
import com.example.aibomm.widget.TodoWidget4x1Provider
import com.example.aibomm.widget.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository by lazy {
        DatabaseProvider.getInstance(application).todoRepository
    }

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private fun refreshWidgets() {
        val context = getApplication<Application>()
        WidgetUpdater.requestUpdateAll(context, TodoWidget1x1Provider::class.java)
        WidgetUpdater.requestUpdateAll(context, TodoWidget2x2Provider::class.java)
        WidgetUpdater.requestUpdateAll(context, TodoWidget4x1Provider::class.java)
    }

    init {
        viewModelScope.launch {
            repository.observeAll().collectLatest { _todos.value = it }
        }
    }

    fun add(title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.add(trimmed)
            refreshWidgets()
        }
    }

    fun toggle(todo: TodoItem) {
        viewModelScope.launch {
            repository.toggle(todo)
            refreshWidgets()
        }
    }

    fun delete(todo: TodoItem) {
        viewModelScope.launch {
            repository.delete(todo)
            refreshWidgets()
        }
    }
}
