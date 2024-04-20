package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlexRateTechExerciseActivity : AppCompatActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var googleTextToSpeechService: GoogleTextToSpeechService
    private lateinit var tvActualWord: TextSwitcher
    private lateinit var tvbrokenDownWord: TextSwitcher
    private lateinit var btnNext: Button
    private val wordList = arrayOf("Electric", "Wonderful", "Analysis", "Energy", "Adventure", "Historical", "Illustrate", "Circumstance", "Fundamental", "Development", "Monumental", "Differential")
    private val brokenDownWordList = arrayOf(
        "E-lec-tric",      // Corresponds to "Electric"
        "Won-der-ful",     // Corresponds to "Wonderful"
        "A-na-ly-sis",     // Corresponds to "Analysis"
        "En-er-gy",        // Corresponds to "Energy"
        "Ad-ven-ture",     // Corresponds to "Adventure"
        "His-tor-i-cal",   // Corresponds to "Historical"
        "Il-lus-trate",    // Corresponds to "Illustrate"
        "Cir-cum-stance",  // Corresponds to "Circumstance"
        "Fun-da-men-tal",  // Corresponds to "Fundamental"
        "De-vel-op-ment",  // Corresponds to "Development"
        "Mon-u-men-tal",   // Corresponds to "Monumental"
        "Dif-fer-en-tial"  // Corresponds to "Differential"
    )
    private val stretchedSyllableList = arrayOf(
        "eeeeel",
        "woonnnn",
        "Annnn",
        "ennnn",
        "Adddddd",
        "hiissss",
        "illll",
        "cirrrrrrr",
        "funnnnnnn",
        "deeeee",
        "monnnnn",
        "diffffff"
    )
    private val remainingWordList = arrayOf(
        "ectric",
        "derful",
        "nalysis",
        "nergy",
        "venture",
        "storical",
        "lustrate",
        "cumstance",
        "damental",
        "velopment",
        "umental",
        "erential"
    )
    private var currentIndex = 0
    private var isTimerStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flex_rate_tech_exercise)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@FlexRateTechExerciseActivity, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }

        googleTextToSpeechService = GoogleTextToSpeechService(this)

        tvActualWord = findViewById<TextSwitcher>(R.id.tvActualWord).apply {
            setFactory {
                TextView(this@FlexRateTechExerciseActivity).apply {
                    textSize = 24f
                    gravity = Gravity.CENTER
                }
            }
            inAnimation = AnimationUtils.loadAnimation(this@FlexRateTechExerciseActivity, android.R.anim.fade_in)
            outAnimation = AnimationUtils.loadAnimation(this@FlexRateTechExerciseActivity, android.R.anim.fade_out)
        }

        tvbrokenDownWord = findViewById<TextSwitcher>(R.id.tvStretchedWord).apply {
            setFactory {
                TextView(this@FlexRateTechExerciseActivity).apply {
                    textSize = 24f
                    gravity = Gravity.CENTER
                }
            }
            inAnimation = AnimationUtils.loadAnimation(this@FlexRateTechExerciseActivity, android.R.anim.fade_in)
            outAnimation = AnimationUtils.loadAnimation(this@FlexRateTechExerciseActivity, android.R.anim.fade_out)
        }

        btnNext = findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            updateWords()
            currentIndex = (currentIndex + 1) % wordList.size
            btnNext.setText("NEXT")


            if (!isTimerStarted) {
                setupTimer()
                isTimerStarted = true
            }
        }

    }

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Flexible Rate Technique")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            databaseReference.updateChildren(exerciseCompletionUpdate).addOnSuccessListener {
                Toast.makeText(this, "Exercise marked as complete.", Toast.LENGTH_SHORT).show()

                XpManager.awardXpForActivity(userId, "FlexibleRateTechnique", 6)
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

    private fun updateWords() {
        val currentWord = wordList[currentIndex]
        val brokenDownWord = brokenDownWordList[currentIndex]
        val stretchedSyllable = stretchedSyllableList[currentIndex]
        val remainingWord = remainingWordList[currentIndex]

        tvActualWord.setText(currentWord)
        tvbrokenDownWord.setText(brokenDownWord)

        // Synthesize the stretched word with a custom SSML
        val ssml = """
        <speak>
            <prosody rate="x-slow" pitch="-2st" volume="loud">$stretchedSyllable</prosody>
            $remainingWord
        </speak>
    """.trimIndent()

        googleTextToSpeechService.synthesizeText(ssml) { audioBytes ->
            runOnUiThread {
                if (audioBytes != null) {
                    playAudio(audioBytes)
                } else {
                    Toast.makeText(this, "Failed to synthesize speech", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAudio(audioData: ByteArray) {

        val mediaPlayer = MediaPlayer().apply {
            try {
                val tempAudioFile = File.createTempFile("tts_output", ".mp3", cacheDir)
                FileOutputStream(tempAudioFile).apply {
                    write(audioData)
                    close()
                }
                setDataSource(tempAudioFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.reset()
                    it.release()
                    tempAudioFile.delete()
                }
            } catch (e: IOException) {
                Log.e("FlexRateTechExercise", "Error playing audio", e)
                runOnUiThread {
                    Toast.makeText(this@FlexRateTechExerciseActivity, "Error playing audio: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupTimer() {
        val timerTextView: TextView = findViewById(R.id.timerTV)
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = " ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerTextView.text = " 00:00s"
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if(userId!= null) {
                 val score = currentIndex.toFloat()
                    saveUserExerciseScore(userId, "FlexibleRateTechnique1", score)
                }
            }
        }
        countDownTimer.start()
    }


    fun saveUserExerciseScore(userId: String, exerciseId: String, score: Float) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val exerciseScore = ExerciseScore(score, currentDate)

        val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
        val userScoreRef = database.getReference("userScores/$userId/$exerciseId")

        userScoreRef.push().setValue(exerciseScore)
            .addOnSuccessListener {

                markExerciseAsComplete()
                // Handle success
                Toast.makeText(this, "score saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}