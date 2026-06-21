package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.BlogItem
import com.example.data.BlogRepository
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BlogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BlogRepository
    val uiState: StateFlow<List<BlogItem>>

    // Simple loading state
    val isGenerating = kotlinx.coroutines.flow.MutableStateFlow(false)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BlogRepository(database.blogDao())

        uiState = repository.allBlogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun generateBlog(topic: String = "आज की ताज़ा ख़बर") {
        viewModelScope.launch {
            isGenerating.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val prompt = """
                    You are an expert Hindi news reporter and blogger, similar to writers on 'Aaj Tak'. 
                    Write a highly engaging, breaking-news style blog article in Hindi about: "$topic".
                    You MUST return exactly a valid JSON object matching this schema:
                    {
                      "title": "A catchy Hindi headline",
                      "snippet": "A 2-line short summary in Hindi",
                      "content": "The full detailed article content in Hindi (at least 3 paragraphs). You can use some line breaks.",
                      "tags": "tag1, tag2, Hindi news"
                    }
                    Do not return any markdown formatting outside the JSON block. Do not include markdown json fences like ```json.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        responseMimeType = "application/json"
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val jsonResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonResponseText != null) {
                    val startIndex = jsonResponseText.indexOf('{')
                    val endIndex = jsonResponseText.lastIndexOf('}')
                    if (startIndex != -1 && endIndex != -1) {
                        val cleanJson = jsonResponseText.substring(startIndex, endIndex + 1)
                        val jsonObject = JSONObject(cleanJson)
                        
                        val newBlog = BlogItem(
                            title = jsonObject.optString("title", "ताज़ा ख़बर"),
                            snippet = jsonObject.optString("snippet", "यहाँ क्लिक करके पूरी ख़बर पढ़ें"),
                            content = jsonObject.optString("content", "सामग्री उपलब्ध नहीं है।"),
                            tags = jsonObject.optString("tags", "News"),
                            timestamp = System.currentTimeMillis()
                        )
                        
                        repository.insert(newBlog)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun deleteBlog(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
