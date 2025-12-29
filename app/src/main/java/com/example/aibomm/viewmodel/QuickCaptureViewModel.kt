package com.example.aibomm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aibomm.data.DatabaseProvider
import com.example.aibomm.data.TodoRepository
import com.example.aibomm.widget.TodoWidget1x1Provider
import com.example.aibomm.widget.TodoWidget2x2Provider
import com.example.aibomm.widget.TodoWidget4x1Provider
import com.example.aibomm.widget.WidgetUpdater
import com.example.aibomm.api.AiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuickCaptureViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository by lazy {
        DatabaseProvider.getInstance(application).todoRepository
    }

    private val aiRepository = AiRepository()

    private fun refreshWidgets() {
        val context = getApplication<Application>()
        WidgetUpdater.requestUpdateAll(context, TodoWidget1x1Provider::class.java)
        WidgetUpdater.requestUpdateAll(context, TodoWidget2x2Provider::class.java)
        WidgetUpdater.requestUpdateAll(context, TodoWidget4x1Provider::class.java)
    }

    fun add(
        title: String,
        content: String = "",
        category: String = "text",
        tags: String = "",
        isAiProcessed: Boolean = false,
        summary: String? = null,
        intent: String? = null,
        intentPayload: String? = null
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return
        viewModelScope.launch {
            repository.add(
                title = trimmedTitle,
                content = content,
                category = category,
                tags = tags,
                isAiProcessed = isAiProcessed,
                summary = summary,
                intent = intent,
                intentPayload = intentPayload
            )
            refreshWidgets()
        }
    }

    fun update(
        id: Long,
        title: String,
        content: String = "",
        category: String = "text",
        tags: String = "",
        isAiProcessed: Boolean = false,
        summary: String? = null,
        intent: String? = null,
        intentPayload: String? = null
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return
        viewModelScope.launch {
            val existing = repository.getById(id) ?: return@launch
            repository.update(
                existing.copy(
                    title = trimmedTitle,
                    content = content,
                    category = category,
                    tags = tags,
                    isAiProcessed = isAiProcessed,
                    summary = summary,
                    intent = intent,
                    intentPayload = intentPayload
                )
            )
            refreshWidgets()
        }
    }

    // Real AI process
    fun processWithAi(text: String, onResult: (String, String, String, String, String?, String?) -> Unit) {
        viewModelScope.launch {
            val result = aiRepository.processNote(text)
            if (result != null) {
                onResult(
                    result.title,
                    result.refinedContent, // The organized text
                    "", // No tags as requested
                    result.intent,
                    result.intent,
                    result.intentPayload?.toString()
                )
            } else {
                // Fallback to simple logic if AI fails
                val title = if (text.length > 20) text.take(20) + "..." else text
                onResult(title, text, "", "text", null, null)
            }
        }
    }
}
