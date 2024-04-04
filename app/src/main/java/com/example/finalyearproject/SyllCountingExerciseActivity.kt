package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyllCountingExerciseActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var currentQuestionIndex: Int = 0
    private lateinit var questionText: TextView
    private lateinit var answersGroup: RadioGroup
    private var currentQuestion: Question? = null
    private lateinit var nextButton: Button
    private val quizQuestions = mutableListOf<Question>()
    private var correctAnswersCount: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var questionCountTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_syl_count_exercise)

        nextButton = findViewById(R.id.next_button)
        questionText = findViewById(R.id.question_text)
        answersGroup = findViewById(R.id.answer_group)
        database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference

        progressBar = findViewById(R.id.progressBar2)
        progressBar.visibility = View.VISIBLE
        questionCountTextView = findViewById(R.id.questionCountTextView)

        loadQuestion()

        nextButton.setOnClickListener {
            // Check that a RadioButton is selected
            if (answersGroup.checkedRadioButtonId != -1) {
                // Proceed with handling the next question
                val selectedId = findViewById<RadioGroup>(R.id.answer_group).checkedRadioButtonId
                val radioButton = findViewById<RadioButton>(selectedId)
                checkAnswer(radioButton.text.toString())
                nextQuestion()
            } else {
                // Optionally show a message prompting the user to make a selection
                showToast("Please select an answer before proceeding.")
            }
        }

        fetchWords()
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= quizQuestions.size) {
            // Handle the end of the quiz
            return
        }

        currentQuestion = quizQuestions[currentQuestionIndex]
        updateQuestionCount()
        displayQuestion()
    }

    private fun displayQuestion() {
        currentQuestion?.let { question ->
            // Set the question text
            questionText.text = question.text

            // Clear previous answers and disable the Next button initially
            answersGroup.removeAllViews()
            nextButton.isEnabled = false

            // Create and add radio buttons for each answer
            question.answers.forEachIndexed { index, answer ->
                val radioButton = RadioButton(this).apply {
                    text = answer
                    id = View.generateViewId()
                }
                answersGroup.addView(radioButton)
            }

            // Set a listener for answer selection
            answersGroup.setOnCheckedChangeListener { group, checkedId ->
                // Enable the Next button when any RadioButton is selected
                nextButton.isEnabled = checkedId != -1
            }
        }
    }

    fun saveUserExerciseScore(userId: String, exerciseId: String, score: Int) {
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

    data class ExerciseScore(
        val score: Int,
        val date: String = ""
    )

    private fun generateQuizQuestions(words: List<WordData>) {
        words.shuffled().take(10).forEach { wordData ->
            val questionText = "How many syllables are in the word '${wordData.word}'?"
            val options = (1..4).toMutableList()
            options.remove(wordData.syllableCount) // Remove correct answer
            options.shuffle()
            options.add(0, wordData.syllableCount) // Add correct answer at the beginning
            options.shuffle() // Shuffle again

            val correctAnswerIndex = options.indexOf(wordData.syllableCount)
            quizQuestions.add(Question(questionText, options.map { it.toString() }, correctAnswerIndex.toString()))
        }
    }

    fun fetchWords() {
        Thread {
            val urls = listOf(
                "https://www.syllablecount.com/syllables/words/1_syllable_words",
                "https://www.syllablecount.com/syllables/words/2_syllable_words",
                "https://www.syllablecount.com/syllables/words/3_syllable_words",
                "https://www.syllablecount.com/syllables/words/4_syllable_words"
            )

            val allWords = mutableListOf<WordData>()

            urls.forEach { url ->
                try {
                    val doc: Document = Jsoup.connect(url).get()
                    val syllableCount = url.split("/").last().split("_")[0].toInt()
                    val words = doc.select("table[id=ctl00_ContentPane_DataList1] a[href]")
                        .mapNotNull { it.text().trim() }
                        .map {
                            Log.d("WordScraping", "Word: $it, Syllables: $syllableCount, URL: $url")
                            WordData(it, syllableCount) }

                    allWords.addAll(words)
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Handle error
                }
            }

            runOnUiThread {
                generateQuizQuestions(allWords)
                loadQuestion()
                progressBar.visibility = View.GONE
            }

        }.start()
    }
    data class WordData(val word: String, val syllableCount: Int)
    private fun checkAnswer(selectedAnswer: String) {
        currentQuestion?.let {
            val correctIndex = it.correctAnswer?.toInt() ?: return
            val correctAnswer = it.answers[correctIndex]

            Log.d("QuizDebug", "Selected: $selectedAnswer, Correct: $correctAnswer")

            if (selectedAnswer.equals(correctAnswer, ignoreCase = true)) {
                showToast("Correct!")
                correctAnswersCount++
            } else {
                showToast("Incorrect. Correct answer is $correctAnswer")
            }
        }
    }

    private fun nextQuestion() {
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)

        questionText.startAnimation(slideOut)
        answersGroup.startAnimation(slideOut)

        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // Increment the question index
                currentQuestionIndex++

                if (currentQuestionIndex >= quizQuestions.size) {

                    finishQuiz()
                    return
                }

                // Load the next question
                loadQuestion()
            }
        })
    }

    private fun finishQuiz() {
        val scoreMessage = "You scored $correctAnswersCount out of 10"

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            AlertDialog.Builder(this)
                .setTitle("Quiz Complete")
                .setMessage(scoreMessage)
                .setPositiveButton("OK") { dialog, which ->
                    markExerciseAsComplete()

                    // Navigate back to ExerciseActivity
                    saveUserExerciseScore(userId, "SyllableCounting1", correctAnswersCount)
                    val intent = Intent(this, ExercisesActivity::class.java)
                    startActivity(intent)
                    finish() // Close the current activity
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateQuestionCount() {
        val totalQuestions = quizQuestions.size
        val currentQuestionNumber = currentQuestionIndex + 1  // +1 because index starts at 0
        questionCountTextView.text = "$currentQuestionNumber/$totalQuestions"
    }

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Syllable Counting")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

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