package com.example.aibomm.api

import com.example.aibomm.BuildConfig
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.SerialName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    private const val BASE_URL = "https://inference.do-ai.run/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val aiService: AiService = retrofit.create(AiService::class.java)
}

class AiRepository {
    private val service = ApiClient.aiService
    private val apiKey = BuildConfig.DO_AI_API_KEY
    private val model = BuildConfig.DO_AI_MODEL

    suspend fun processNote(content: String): AiProcessResult? {
        if (apiKey.isBlank()) return null

        val prompt = """
            你是一个AI闪念胶囊助手。请根据用户的输入内容（可能是凌乱的语音转文字或简短碎片），直接进行整理和润色，生成一个结构清晰、逻辑通顺的总结。
            
            任务要求：
            1. 整理内容：修正语法错误，补全省略信息，使其成为一段专业的笔记。
            2. 生成标题：提取一个20字以内的核心标题。
            3. 识别意图：判断用户是否想创建待办事项(todo)、日程(calendar)、保存链接(link)或仅仅是记录信息(info)。
            4. 提取信息：如果是日程或待办，提取时间地点等关键要素。

            输入内容：
            $content

            请严格以 JSON 格式返回结果，不要包含任何标签提取任务，格式如下：
            {
              "title": "...",
              "refined_content": "这里填写整理润色后的详细内容...",
              "summary": "这里填写一句话的核心总结...",
              "intent": "...",
              "intentPayload": "..."
            }
        """.trimIndent()

        return try {
            val response = service.chatCompletions(
                authorization = "Bearer $apiKey",
                request = ChatRequest(
                    model = model,
                    messages = listOf(
                        ChatMessage(role = "system", content = "你是一个擅长笔记整理和文字润色的专家。"),
                        ChatMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    maxTokens = 1000
                )
            )

            val aiResultJson = response.choices.firstOrNull()?.message?.content ?: return null
            android.util.Log.d("AiRepository", "Raw AI Response: $aiResultJson")
            
            val jsonRegex = """\{[\s\S]*\}""".toRegex()
            val matchResult = jsonRegex.find(aiResultJson)
            val cleanJson = matchResult?.value ?: aiResultJson.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            android.util.Log.d("AiRepository", "Cleaned JSON: $cleanJson")
            Json.decodeFromString<AiProcessResult>(cleanJson)
        } catch (e: Exception) {
            android.util.Log.e("AiRepository", "Error processing AI response", e)
            null
        }
    }
}

@kotlinx.serialization.Serializable
data class AiProcessResult(
    val title: String,
    @SerialName("refined_content") val refinedContent: String,
    val summary: String,
    val intent: String,
    @SerialName("intentPayload") val intentPayload: JsonElement? = null
)
