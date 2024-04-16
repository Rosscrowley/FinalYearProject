package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ReadingExerInfo : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reading_exer_info_page)

        val nextButton: ImageButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this@ReadingExerInfo, ChooseReadingTopicActivity::class.java)
            startActivity(intent)
            finish()
        }

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@ReadingExerInfo, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }



    }
}