package com.example.finalyearproject

import android.content.Context
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.AudioConfig
import com.google.cloud.texttospeech.v1.AudioEncoding
import com.google.cloud.texttospeech.v1.SsmlVoiceGender
import com.google.cloud.texttospeech.v1.SynthesisInput
import com.google.cloud.texttospeech.v1.TextToSpeechClient
import com.google.cloud.texttospeech.v1.TextToSpeechSettings
import com.google.cloud.texttospeech.v1.VoiceSelectionParams
import com.google.protobuf.ByteString
import java.io.IOException

class TextToSpeechService(context: Context) {
    private var textToSpeechClient: TextToSpeechClient? = null

    init {
        try {
            // Load the service account key from the assets directory
            val credentialsStream = context.assets.open("text_to_speech_key.json")

            // Create GoogleCredentials from the service account key
            val credentials = GoogleCredentials.fromStream(credentialsStream)

            // Set the credentials for TextToSpeechClient
            textToSpeechClient = TextToSpeechClient.create(
                TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build()
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun convertTextToSpeech(textToConvert: String?, languageCode: String?): ByteString? {
        return try {
            // Build the synthesis input
            val input = SynthesisInput.newBuilder().setText(textToConvert).build()
            val voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setName("en-US-Wavenet-D")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build()
            val audioConfig =
                AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).build()

            // Perform the text-to-speech request
            val response = textToSpeechClient!!.synthesizeSpeech(input, voice, audioConfig)

            // Return the audio content as ByteString
            ByteString.copyFrom(response.audioContent.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            null // Handle the exception and return appropriate value
        }
    }

    fun close() {
        textToSpeechClient?.close()
    }
}