package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var pronunCard: CardView
    private lateinit var exerciseCard: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        pronunCard = findViewById(R.id.pronunTest)
        pronunCard.setOnClickListener{
            val intent = Intent(this@MainActivity, SpeechActivity::class.java)
            startActivity(intent)
        }

        exerciseCard = findViewById(R.id.exercisesCard)
        exerciseCard.setOnClickListener{
            val intent = Intent(this@MainActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }

    }
}

