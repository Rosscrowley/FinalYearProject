package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class WaveAnalysisPageActivity : AppCompatActivity() {

    private lateinit var compareCard: CardView
    private lateinit var voiceRecordingCard: CardView
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave_analysis_page)

        compareCard = findViewById(R.id.audioWaveComparison)
        compareCard.setOnClickListener{
            val intent = Intent(this@WaveAnalysisPageActivity, WordListActivity::class.java)
            startActivity(intent)
        }

        voiceRecordingCard = findViewById(R.id.recordingCard)
        voiceRecordingCard.setOnClickListener{
            val intent = Intent(this@WaveAnalysisPageActivity, VoiceRecordListActivity::class.java)
            startActivity(intent)
        }



        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.menu_item_wave_analysis
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_home -> {
                    // Navigate to Home
                    val intent = Intent(this@WaveAnalysisPageActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_progress -> {
                    // Navigate to Progress
                    val intent = Intent(this@WaveAnalysisPageActivity, ProgressActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_exercise -> {
                    // Navigate to Exercise
                    val intent = Intent(this@WaveAnalysisPageActivity, ExercisesActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}