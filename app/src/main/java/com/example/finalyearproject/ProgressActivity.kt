package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProgressActivity : AppCompatActivity() {

    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_page)

        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView)
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        exercisesRecyclerView.addItemDecoration(dividerItemDecoration)

        val exercises = listOf(Exercise("Reading1", "Reading Progress"), Exercise("TongueTwister1", "Tongue Twister Progress"), Exercise("SyllableCounting1", "Syllable Counting Progress")) // Example data
        exercisesRecyclerView.adapter = ExerciseAdapter(exercises) { exercise ->
            val intent = Intent(this, ExerciseProgressGraphActivity::class.java)
            intent.putExtra("EXERCISE_ID", exercise.id)
            startActivity(intent)
        }

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.menu_item_progress
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_home -> {
                    // Navigate to Home
                    val intent = Intent(this@ProgressActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_exercise -> {
                    // Navigate to Exercise
                    val intent = Intent(this@ProgressActivity, ExercisesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_wave_analysis -> {
                    // Navigate to Exercise
                    val intent = Intent(this@ProgressActivity, WaveAnalysisPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}