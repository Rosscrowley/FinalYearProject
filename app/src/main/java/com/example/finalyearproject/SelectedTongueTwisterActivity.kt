package com.example.finalyearproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.protobuf.ByteString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class SelectedTongueTwisterActivity : AppCompatActivity() {

    private lateinit var tongueTwisterContentTextView: TextView
    private var textToSpeechService: TextToSpeechService? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var buttonListen: Button
    private lateinit var startButton: Button
    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var dafService: DafService
    private var isRecording = false
    private val sampleRate = 44100
    private val delayMillis = 500
    private lateinit var dafIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_selected_tongue_twister)
        dafIcon = findViewById(R.id.dafIcon)
        dafIcon.isVisible = false

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@SelectedTongueTwisterActivity, ChooseTongueTwisterActivity::class.java)
            startActivity(intent)
        }

        textToSpeechService = TextToSpeechService(this)

        tongueTwisterContentTextView = findViewById(R.id.selectedTongueTwisterContentTextView)

        // Retrieve the content from the intent
        val intent = intent
        val tongueTwisterContent = intent.getStringExtra(ChooseTongueTwisterActivity.TONGUE_TWISTER_CONTENT)
        Log.d("SelectedTongueTwisterActivity", "Received content: $tongueTwisterContent")

        tongueTwisterContentTextView.text = tongueTwisterContent

        buttonListen = findViewById<Button>(R.id.listenButton)


        buttonListen.setOnClickListener(View.OnClickListener {
            TextToSpeechTask().execute(tongueTwisterContent)
        })

        startButton = findViewById(R.id.tryButton)

        startButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (checkPermission()) {
                    startRecordingAndPlayback()
                } else {
                    requestPermissions()
                }
            }
        }
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

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
            TextPassageActivity.REQUEST_AUDIO_PERMISSION_CODE
        )
    }
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                dafService.startRecording()
            } else {
                // Handle the case where the user denies the permission.
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecordingAndPlayback() {
        isRecording = true
        startButton.text = "Stop"
        dafIcon.isVisible = true
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val bufferSize = minBufferSize * 2
        val circularBuffer = TextPassageActivity.CircularBuffer(bufferSize * delayMillis / 1000)

        thread {
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioRecord.startRecording()
            audioTrack.play()

            val tempBuffer = ShortArray(minBufferSize)

            while (isRecording) {
                val readSize = audioRecord.read(tempBuffer, 0, minBufferSize)
                if (readSize > 0) {
                    tempBuffer.forEach { circularBuffer.write(it) }

                    while (!circularBuffer.isEmpty) {
                        audioTrack.write(shortArrayOf(circularBuffer.read()), 0, 1)
                    }
                }
            }

            audioRecord.stop()
            audioRecord.release()
            audioTrack.stop()
            audioTrack.release()
        }
    }

    private fun stopRecording() {
        isRecording = false
        startButton.text = "Start"
        dafIcon.isVisible = false

        showRatingDialog()
    }

    private fun showRatingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Submit") { dialog, which ->
                    val rating = ratingBar.rating
                    // Save the score after the user submits the rating
                    saveUserExerciseScore(userId, "TongueTwister1", rating)
                    Toast.makeText(this, "Rating: $rating", Toast.LENGTH_SHORT).show()
                    markExerciseAsComplete()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            // Handle the case where there is no signed-in user
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }
    fun saveUserExerciseScore(userId: String, exerciseId: String, score: Float) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val exerciseScore = ExerciseScore(score, currentDate)

        val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
        val userScoreRef = database.getReference("userScores/$userId/$exerciseId")

        userScoreRef.push().setValue(exerciseScore)
            .addOnSuccessListener {
                // Handle success
                Toast.makeText(this, "score saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Tongue Twisters")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            XpManager.awardXpForActivity(userId, "tongueTwisterExercise", 3)

            databaseReference.updateChildren(exerciseCompletionUpdate).addOnSuccessListener {
                Toast.makeText(this, "Exercise marked as complete.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to mark exercise as complete: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}