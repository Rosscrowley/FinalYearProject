package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExercisesActivity : AppCompatActivity() {

    private lateinit var readingCard: CardView
    private lateinit var breathingCard: CardView
    private lateinit var tonTwistCard: CardView
    private lateinit var syllCountCard: CardView
    private lateinit var progressiveMuscleCard: CardView
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)


        readingCard = findViewById(R.id.readingCard)
        readingCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, DAFInfo::class.java)
            startActivity(intent)
        }
        breathingCard = findViewById(R.id.breathingCard)
        breathingCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, BreathingExerciseInfoPage::class.java)
            startActivity(intent)
        }
        tonTwistCard = findViewById(R.id.tongueTwistCard)
        tonTwistCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, ChooseTongueTwisterActivity::class.java)
            startActivity(intent)
        }
        syllCountCard = findViewById(R.id.syllableCountingCard)
        syllCountCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, SyllCountInfo::class.java)
            startActivity(intent)
        }
        progressiveMuscleCard = findViewById(R.id.muscleExerciseCard)
        progressiveMuscleCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, ProgressiveMuscleRelaxationInfoPage::class.java)
            startActivity(intent)
        }


        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.menu_item_exercise
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_home -> {
                    // Navigate to Home
                    val intent = Intent(this@ExercisesActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_progress -> {
                    // Navigate to Progress
                    val intent = Intent(this@ExercisesActivity, ProgressActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_wave_analysis -> {
                    // Navigate to Exercise
                    val intent = Intent(this@ExercisesActivity, WaveAnalysisPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }
}