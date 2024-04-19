package com.example.finalyearproject
import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GoogleTextToSpeechService(private val context: Context) {
    private val client = OkHttpClient()
    private val apiUrl = "https://texttospeech.googleapis.com/v1/text:synthesize"

    fun synthesizeText(ssml: String, callback: (result: ByteArray?) -> Unit) {
        Thread {
            try {
                val credentials = loadCredentials()
                val accessToken = credentials.refreshAccessToken().tokenValue

                val json = JSONObject().apply {
                    put("input", JSONObject().put("ssml", ssml))
                    put("voice", JSONObject().put("languageCode", "en-US").put("ssmlGender", "NEUTRAL"))
                    put("audioConfig", JSONObject().put("audioEncoding", "MP3"))
                }
                val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("TTS", "Failed to send request: $e")
                        callback(null)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!it.isSuccessful) {
                                Log.e("TTS", "Failed response: ${it.body?.string()}")
                                callback(null)
                            } else {
                                val responseBody = it.body?.string()
                                Log.d("TTS", "Successful response: $responseBody")
                                val audioContent = JSONObject(responseBody ?: "").getString("audioContent")
                                val audioBytes = Base64.decode(audioContent, Base64.DEFAULT)
                                callback(audioBytes)
                            }
                        }
                    }
                })
            } catch (e: IOException) {
                Log.e("TTS", "Failed to load credentials: $e")
                callback(null)
            }
        }.start()
    }

    @Throws(IOException::class)
    private fun loadCredentials(): GoogleCredentials {
        context.assets.open("wise-brook-405117-637121642502.json").use { inputStream ->
            return GoogleCredentials.fromStream(inputStream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
        }
    }
}