package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiTask(val title: String, val promptInstruction: String) {
  SUMMARIZE(
    title = "Summarize Note",
    promptInstruction = "Provide a concise summary of the following note in 2-3 bullet points followed by a one-sentence key takeaway."
  ),
  REWRITE(
    title = "Polish & Rewrite",
    promptInstruction = "Rewrite and polish the following note text for superior clarity, readability, and a clean professional tone while keeping all facts intact."
  ),
  ACTION_POINTS(
    title = "Extract Action Points",
    promptInstruction = "Extract actionable to-dos, checklists, or key action steps from the following note. Format each item as a checklist item: '- [ ] task description'."
  )
}

sealed class AiResult {
  data class Success(val text: String, val isLiveAi: Boolean) : AiResult()
  data class Error(val message: String) : AiResult()
  data object NotConfigured : AiResult()
}

class AiService {
  private val client: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  fun isConfigured(): Boolean {
    return try {
      BuildConfig.GEMINI_API_KEY.isNotBlank() &&
          BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (e: Throwable) {
      false
    }
  }

  suspend fun processNote(
    task: AiTask,
    title: String,
    content: String
  ): AiResult = withContext(Dispatchers.IO) {
    if (content.isBlank() && title.isBlank()) {
      return@withContext AiResult.Error("Note is empty. Add some content before analyzing with AI.")
    }

    val apiKey = try {
      BuildConfig.GEMINI_API_KEY.trim()
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext AiResult.NotConfigured
    }

    val notePayload = buildString {
      if (title.isNotBlank()) appendLine("Title: $title")
      appendLine("Content:\n$content")
    }

    val prompt = "${task.promptInstruction}\n\nNote:\n$notePayload"

    try {
      val url =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

      val jsonBody = JSONObject().apply {
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val textPart = JSONObject().put("text", prompt)
        partsArray.put(textPart)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        put("contents", contentsArray)
      }

      val request = Request.Builder()
        .url(url)
        .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        val errorMsg = try {
          val errorJson = JSONObject(responseBody).optJSONObject("error")
          errorJson?.optString("message") ?: "API Error (${response.code})"
        } catch (e: Exception) {
          "API call failed with status ${response.code}"
        }
        return@withContext AiResult.Error(errorMsg)
      }

      val responseJson = JSONObject(responseBody)
      val candidates = responseJson.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        return@withContext AiResult.Error("No response candidates returned by Gemini.")
      }

      val firstCandidate = candidates.getJSONObject(0)
      val contentCandidate = firstCandidate.optJSONObject("content")
      val parts = contentCandidate?.optJSONArray("parts")
      val outputText = parts?.optJSONObject(0)?.optString("text")

      if (outputText.isNullOrBlank()) {
        return@withContext AiResult.Error("Empty response text from AI service.")
      }

      return@withContext AiResult.Success(text = outputText.trim(), isLiveAi = true)
    } catch (e: Exception) {
      return@withContext AiResult.Error(e.localizedMessage ?: "Failed to reach AI service. Please check network connection.")
    }
  }
}
