package com.newbiechen.inkreader.core.ai

import com.newbiechen.inkreader.BuildConfig
import com.newbiechen.inkreader.core.ai.models.AIQuestionRequest
import com.newbiechen.inkreader.core.ai.models.AIQuestionResponse
import com.newbiechen.inkreader.core.ai.models.AIQuestionType
import com.newbiechen.inkreader.data.remote.api.OpenAIApiService
import com.newbiechen.inkreader.data.remote.models.ChatCompletionRequest
import com.newbiechen.inkreader.data.remote.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Question Service Interface
 * Defines the contract for AI-powered question answering
 */
interface AIQuestionService {
    
    /**
     * Ask AI a question based on selected text
     * @param request The AI question request containing text and question type
     * @return AI response or error result
     */
    suspend fun askQuestion(request: AIQuestionRequest): Result<AIQuestionResponse>
    
    /**
     * Detect the most appropriate question type for the given text
     * @param selectedText The text selected by user
     * @return Detected question type
     */
    fun detectQuestionType(selectedText: String): AIQuestionType
}

/**
 * AI Question Service Implementation
 * Handles OpenAI API integration and question processing
 */
@Singleton
class AIQuestionServiceImpl @Inject constructor(
    private val openAIApiService: OpenAIApiService
) : AIQuestionService {
    
    companion object {
        // Read API key from BuildConfig (configured in gradle.properties)
        private val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY.ifEmpty { "YOUR_API_KEY_HERE" }
        private val AUTHORIZATION_HEADER = "Bearer $OPENAI_API_KEY"
        
        // Prompt templates for different question types
        private const val CONCEPT_EXPLANATION_PROMPT = """
你是一个专业的知识解释助手。请对以下选中的文本内容进行概念解释：

选中文本："{selectedText}"
上下文："{context}"

请用简洁明了的语言解释其中涉及的核心概念，字数控制在200字以内。
"""
        
        private const val TRANSLATION_PROMPT = """
你是一个专业的翻译助手。请翻译以下选中的文本：

选中文本："{selectedText}"

如果是中文，请翻译成英文；如果是英文，请翻译成中文。提供准确、自然的翻译。
"""
        
        private const val WORD_LEARNING_PROMPT = """
你是一个英语学习助手。请对以下选中的英语内容进行学习指导：

选中文本："{selectedText}"

请提供：
1. 核心词汇的中文释义
2. 重要语法点解析
3. 使用场景说明
字数控制在250字以内。
"""
        
        private const val SUMMARY_PROMPT = """
你是一个内容总结助手。请对以下选中的文本进行总结：

选中文本："{selectedText}"
上下文："{context}"

请提取核心要点，用简洁的语言进行总结，字数控制在150字以内。
"""
        
        private const val EXPANSION_PROMPT = """
你是一个知识扩展助手。基于以下选中的文本，提供相关的扩展阅读建议：

选中文本："{selectedText}"
上下文："{context}"

请推荐2-3个相关的学习方向或扩展话题，并简要说明原因，字数控制在200字以内。
"""
    }
    
    override suspend fun askQuestion(request: AIQuestionRequest): Result<AIQuestionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(request.questionType, request.selectedText, request.context)
                val chatRequest = ChatCompletionRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        ChatMessage(role = "user", content = prompt)
                    ),
                    maxTokens = 800,
                    temperature = 0.7
                )
                
                val response = openAIApiService.createChatCompletion(
                    authorization = AUTHORIZATION_HEADER,
                    request = chatRequest
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.choices.isNotEmpty()) {
                        val aiResponse = AIQuestionResponse(
                            id = body.id,
                            questionType = request.questionType,
                            selectedText = request.selectedText,
                            answer = body.choices[0].message.content.trim(),
                            timestamp = System.currentTimeMillis(),
                            usage = body.usage
                        )
                        Result.success(aiResponse)
                    } else {
                        Result.failure(Exception("Empty response from OpenAI API"))
                    }
                } else {
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: HttpException) {
                Result.failure(Exception("API error: ${e.message()}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }
    
    override fun detectQuestionType(selectedText: String): AIQuestionType {
        val text = selectedText.trim().lowercase()
        
        return when {
            // Check if text contains mostly English words - suitable for word learning
            text.matches(Regex("[a-z\\s.,!?;:\"'-]+")) && text.split("\\s+".toRegex()).size <= 20 -> {
                AIQuestionType.WORD_LEARNING
            }
            
            // Check if text is mixed Chinese-English - suitable for translation
            text.contains(Regex("[\\u4e00-\\u9fff]")) && text.contains(Regex("[a-zA-Z]")) -> {
                AIQuestionType.TRANSLATION
            }
            
            // Check if text is purely English - suitable for translation
            text.matches(Regex("[a-zA-Z\\s.,!?;:\"'-]+")) -> {
                AIQuestionType.TRANSLATION
            }
            
            // Check if text is long - suitable for summary
            text.length > 200 -> {
                AIQuestionType.SUMMARY
            }
            
            // Default to concept explanation for other cases
            else -> AIQuestionType.CONCEPT_EXPLANATION
        }
    }
    
    private fun buildPrompt(
        questionType: AIQuestionType,
        selectedText: String,
        context: String?
    ): String {
        val template = when (questionType) {
            AIQuestionType.CONCEPT_EXPLANATION -> CONCEPT_EXPLANATION_PROMPT
            AIQuestionType.TRANSLATION -> TRANSLATION_PROMPT
            AIQuestionType.WORD_LEARNING -> WORD_LEARNING_PROMPT
            AIQuestionType.SUMMARY -> SUMMARY_PROMPT
            AIQuestionType.EXPANSION -> EXPANSION_PROMPT
        }
        
        return template
            .replace("{selectedText}", selectedText)
            .replace("{context}", context ?: "")
    }
} 