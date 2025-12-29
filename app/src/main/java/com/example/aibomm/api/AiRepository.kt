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
            你是一个 AI 闪念胶囊助手，包含两个核心模块：
            1. 智能整理者 (The Organizer Agent)：
               - 自动排版：修正标点，通过换行和缩进优化可读性。
               - 摘要生成：如果字数 > 100字，自动在顶部生成一句话 TL;DR。
               - 标签预测：分析内容，自动预测标签（如：灵感、工作、生活、日程等）。
            2. 行动路由 (The Action Router)：
               - 识别用户意图，并将其映射为功能调用。
               - 支持的指令意图：
                 - "calendar" -> 创建日程（识别时间地点）。
                 - "alarm" -> 设置闹钟（识别时间）。
                 - "message" -> 发送消息（识别联系人和内容）。
                 - "info" -> 普通记录。

            输入内容：
            $content

            请严格以 JSON 格式返回结果，格式如下：
            {
              "title": "20字以内的核心标题",
              "refined_content": "整理润色排版后的完整内容，包含 TL;DR (如果字数多)",
              "summary": "简短的摘要或润色后的主文本",
              "tags": "标签1,标签2",
              "intent": "calendar | alarm | message | info",
              "intentPayload": {
                  "time": "ISO格式时间(如有)",
                  "location": "地点(如有)",
                  "contact": "联系人(如有)",
                  "message": "内容(如有)",
                  "raw_text": "原始意图描述"
              }
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
    val tags: String = "",
    val intent: String,
    @SerialName("intentPayload") val intentPayload: JsonElement? = null
)
