package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ExercisesActivity : AppCompatActivity() {

    private lateinit var readingCard: CardView
    private lateinit var breathingCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)


        readingCard = findViewById(R.id.readingCard)
        readingCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, ChooseReadingTopicActivity::class.java)
            startActivity(intent)
        }
        breathingCard = findViewById(R.id.breathingCard)
        breathingCard.setOnClickListener{
            val intent = Intent(this@ExercisesActivity, BreathingExerciseActivity::class.java)
            startActivity(intent)
        }

    }
}