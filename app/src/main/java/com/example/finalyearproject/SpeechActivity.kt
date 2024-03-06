package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.protobuf.ByteString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

 class  SpeechActivity : AppCompatActivity(){
    private val RQ_SPEECH_REC = 102
    private lateinit var bottomNavigationView: BottomNavigationView

   // private val audioWave: AudioWaveformView = TODO()
     private lateinit var buttonListen: Button
    private lateinit var enteredText: EditText
     private var mediaPlayer: MediaPlayer? = null
     private var text: String? = null
     private var textToSpeechService: TextToSpeechService? = null
     private lateinit var visualizerView: VisualizerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        textToSpeechService = TextToSpeechService(this)

        buttonListen = findViewById<Button>(R.id.listenB)
        enteredText = findViewById<EditText>(R.id.enteredText)

        //buttonListen!!.isEnabled = false

        buttonListen.setOnClickListener(View.OnClickListener {
            text = enteredText.text.toString()
            TextToSpeechTask().execute(text)
        })

        val btn = findViewById<Button>(R.id.practiceB)
        btn.setOnClickListener {

            askSpeechInput()
        }

    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val textV = findViewById<TextView>(R.id.saidText)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK){
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0).toString()

            val originalText = enteredText!!.text.toString()
            val highlightedText = highlightDifferences(originalText, spokenText)

            textV.text = highlightedText
        }
    }

    private fun askSpeechInput() {

        if(!SpeechRecognizer.isRecognitionAvailable(this)){
            Toast.makeText(this,"Speech recognition is not available", Toast.LENGTH_SHORT).show()
        }else{
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
            startActivityForResult(i,RQ_SPEECH_REC)
        }

    }

  //   private fun visualizeAudioWaveform(spokenText: String) {
    //     val audioAmplitudes = calculateAudioAmplitudes(spokenText)
     //    audioWave.setAudioAmplitudes(audioAmplitudes)

     //}

   //  private fun calculateAudioAmplitudes(spokenText: String) {

     //}

     fun highlightDifferences(original: String, modified: String): SpannableStringBuilder {
         val spannableStringBuilder = SpannableStringBuilder()
         val minLength = minOf(original.length, modified.length)

         for (i in 0 until minLength) {

             val originalChar = original[i].lowercaseChar()
             val modifiedChar = modified[i].lowercaseChar()
             if (originalChar != modifiedChar) {
                 // Highlight differing character in red
                 spannableStringBuilder.append(modified[i].toString(), ForegroundColorSpan(Color.RED), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
             } else {
                 spannableStringBuilder.append(original[i].toString(), ForegroundColorSpan(Color.GREEN), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
             }
         }

         // Append remaining characters from the longer string
         if (original.length > minLength) {
             spannableStringBuilder.append(original.substring(minLength))
         } else if (modified.length > minLength) {
             spannableStringBuilder.append(modified.substring(minLength), ForegroundColorSpan(Color.RED), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
         }

         return spannableStringBuilder
     }

     private inner class TextToSpeechTask : AsyncTask<String?, Void?, ByteString?>() {
         var languageCode = "en-US"
         override  fun doInBackground(vararg params: String?): ByteString? {
             val textToConvert = params[0] ?: return null

             if (textToSpeechService == null) {
                 // Handle the case where textToSpeechService is null
                 return null
             }

             return textToSpeechService!!.convertTextToSpeech(textToConvert, languageCode)
         }

         override fun onPostExecute(audioContents: ByteString?) {
             if (audioContents != null) {
                 playAudio(audioContents.toByteArray())
             } else {
                 // Handle the case where the API call fails
             }
         }
     }

     private fun playAudio(audioData: ByteArray) {
         try {
             // Check if MediaPlayer is currently playing, and stop it if needed
             if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                 mediaPlayer!!.stop()
                 mediaPlayer!!.reset()
             }
             val tempAudioFile = saveToTempFile(audioData)
             // Initialize MediaPlayer
             mediaPlayer = MediaPlayer()

             // Set audio attributes to ensure proper playback on different Android versions
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                 mediaPlayer!!.setAudioAttributes(
                     AudioAttributes.Builder()
                         .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                         .setUsage(AudioAttributes.USAGE_MEDIA)
                         .build()
                 )
             } else {
                 mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
             }

             // Set the audio data to MediaPlayer
             mediaPlayer!!.setDataSource(tempAudioFile.absolutePath)

             // Prepare and start playback
             mediaPlayer!!.prepare()
             mediaPlayer!!.start()

             // Set a listener to handle when playback completes
             mediaPlayer!!.setOnCompletionListener {
                 // Handle any post-completion actions if needed
             }
         } catch (e: IOException) {
             Log.e("playAudio", "Error playing audio", e)
         }
     }

     @Throws(IOException::class)
     private fun saveToTempFile(audioData: ByteArray): File {
         val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
         val fos = FileOutputStream(tempFile)
         fos.write(audioData)
         fos.close()
         return tempFile
     }

     override fun onDestroy() {
         super.onDestroy()
         if (mediaPlayer != null) {
             mediaPlayer!!.release()
             mediaPlayer = null
         }
     }
}
