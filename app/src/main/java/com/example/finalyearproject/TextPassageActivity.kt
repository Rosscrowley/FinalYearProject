package com.example.finalyearproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.finalyearproject.ChooseReadingTopicActivity.Companion.EXTRA_CATEGORY_NAME
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class TextPassageActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
    private var amplitudes: ArrayList<Float>? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private var isRecording = false
    private var isRecordingDAF = false
    private val sampleRate = 44100
    private val delayMillis = 500
    private lateinit var startButton: Button
    private lateinit var dafIcon: ImageView
    private lateinit var ratingBar: RatingBar
    private lateinit var progressBar: ProgressBar
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private lateinit var db : AppDatabase
    private var recordingStartTime: Long = 0

    private var dirPath = ""
    private var filename = ""


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager2)

        db = DatabaseManager.getDatabase(this)

        dafIcon = findViewById(R.id.dafIcon)
        dafIcon.isVisible = false

        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)
        viewPager = findViewById(R.id.viewPager_passages)

        progressBar = findViewById(R.id.progressBar2)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@TextPassageActivity, ChooseReadingTopicActivity::class.java)
            startActivity(intent)
        }

        viewPager.adapter = PassagesAdapter(this, emptyList())

        getPassagesForCategory(categoryName) { passages ->

            runOnUiThread {
                viewPager.adapter = PassagesAdapter(this, passages)
            }
        }
        startButton = findViewById(R.id.startRecButton)
        startButton.setOnClickListener {
                toggleRecording()
            }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun toggleRecording() {
        if (!checkPermission()) {
            requestPermissions()
            return
        }

        if (isRecording && isRecordingDAF) {
            stopRecordingAndPlayBack()
            stopRecording()
            isRecording = false
            isRecordingDAF = false
            startButton.text = "Start Recording"
            dafIcon.isVisible = false
            showSaveOptionDialog()
        } else {
            startRecording()
            startRecordingAndPlayback()
            isRecording = true
            isRecordingDAF = true
            startButton.text = "Stop Recording"
            dafIcon.isVisible = true
        }
    }

    private fun showSaveOptionDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Save Recording")
            .setMessage("Enter a name for the recording:")
            .setView(input)
            .setPositiveButton("Save") { dialog, which ->
                val recordingName = input.text.toString()
                Log.d("VoiceRecordListActivity", "Saving recording with name: $recordingName")
                saveRecording(recordingName) // Pass the recording name to the save method
                showRatingDialog()
            }
            .setNegativeButton("Discard") { dialog, which ->
                Log.d("VoiceRecordListActivity", "Discarding recording.")
                showRatingDialog()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun saveRecording(recordingName: String) {
        val recordingEndTime = System.currentTimeMillis() // Capture end time
        val duration = recordingEndTime - recordingStartTime
        val newFilename = recordingName
        Log.d("VoiceRecordListActivity", "Attempting to save recording: $newFilename")


        if(newFilename != filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
            Log.d("VoiceRecordListActivity", "File renamed: $newFilename")
        }

        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time
        var ampsPath = "$dirPath$newFilename"
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        try{
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
            Log.d("VoiceRecordListActivity", "Amplitudes saved successfully.")
        }catch (e :IOException){
            Log.e("VoiceRecordListActivity", "Failed to save amplitudes", e)
        }

        if (userId != null) {
            var record = AudioRecord(newFilename, filePath, timestamp, ampsPath, userId, duration)
            Log.d("VoiceRecordListActivity", "Recording saved with duration: $duration ms")
            GlobalScope.launch {
                db.audioRecordingDao().insertRecording(record)
                Log.d("VoiceRecordListActivity", "Recording saved with ID: $userId")
            }
        }else {
            Log.d("VoiceRecordListActivity", "User ID is null, recording not saved.")
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        Log.d("Recording", "Is recording")

        recordingStartTime = System.currentTimeMillis()

        isRecording = true
        dirPath = "${externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        mediaRecorder = MediaRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("AudioRecord", "prepare() failed")
            }

            start()
        }
    }
    @SuppressLint("MissingPermission")
    private fun startRecordingAndPlayback() {
        isRecordingDAF = true
        startButton.text = "Stop"
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val bufferSize = minBufferSize * 2
        val circularBuffer = CircularBuffer(bufferSize * delayMillis / 1000)

        thread {
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioRecord.startRecording()
            audioTrack.play()

            val tempBuffer = ShortArray(minBufferSize)

            while (isRecordingDAF) {
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
        isRecordingDAF = false
    }

    private fun stopRecordingAndPlayBack() {
        // Stop and release MediaRecorder
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null // Reset the mediaRecorder


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
                    saveUserExerciseScore(userId, "Reading1", rating)
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

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Reading Comprehension")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            XpManager.awardXpForActivity(userId, "readingExercise", 3)

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

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_AUDIO_PERMISSION_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toggleRecording() // Try toggling recording again now that permission is granted
                } else {
                    // Inform the user that permission was not granted
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun getPassagesForCategory(categoryName: String?, callback: (List<String>) -> Unit) {
        val supportedCategories = listOf("sport", "business", "entertainment", "culture", "news")

        if (categoryName in supportedCategories) {
            fetchHeadlines(categoryName ?: "sport", callback)
        } else {
            // Your existing Firebase code
            progressBar.visibility = View.GONE
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference
            val passagesReference = databaseReference.child("reading_texts").child(categoryName ?: "")

            passagesReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val passages = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.getValue(String::class.java)?.let {
                            passages.add(it)
                        }
                    }
                    callback(passages)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("PassagesActivity", "Error fetching data: ${databaseError.message}")
                }
            })
        }
    }

    fun fetchHeadlines(category: String, callback: (List<String>) -> Unit) {

        val baseUrl = "https://www.rte.ie"
        val categoryUrl = "$baseUrl/$category/"
        progressBar.visibility = View.VISIBLE

        thread {
            try {
                val document: Document = Jsoup.connect(categoryUrl).get()
                val headlines = mutableListOf<String>()

                val headlineElements = document.select("div.article-meta a")
                val headlineUrls = headlineElements.take(3).map { element ->
                    baseUrl + element.attr("href")
                }

                headlineUrls.forEach { url ->
                    fetchArticleContent(url) { articleText ->
                        headlines.add(articleText)
                        if (headlines.size == headlineUrls.size) {
                            callback(headlines)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("fetchHeadlines", "Error in category $category: ${e.message}")
            }
        }
    }
    fun fetchArticleContent(url: String, callback: (String) -> Unit) {

        thread {
            try {
                val articleDocument: Document = Jsoup.connect(url).get()
                val content = articleDocument.select("section.article-body")
                val articleText = content.text()

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    callback(articleText)
                    // Any additional UI updates related to this article
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("fetchArticleContent", "Error fetching article content: ${e.message}")
            }
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

    data class Headline(val title: String, val url: String)
    class CircularBuffer(size: Int) {
        private val buffer = ShortArray(size)
        private var writeIndex = 0
        private var readIndex = 0

        val isFull: Boolean
            get() = (writeIndex + 1) % buffer.size == readIndex

        val isEmpty: Boolean
            get() = writeIndex == readIndex

        fun write(value: Short) {
            if (!isFull) {
                buffer[writeIndex] = value
                writeIndex = (writeIndex + 1) % buffer.size
            }
        }

        fun read(): Short {
            return if (!isEmpty) {
                val value = buffer[readIndex]
                readIndex = (readIndex + 1) % buffer.size
                value
            } else {
                0
            }
        }
    }

}






