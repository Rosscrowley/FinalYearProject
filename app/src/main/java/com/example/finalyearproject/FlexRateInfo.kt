package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class FlexRateInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flex_rate_info)

        val nextButton: ImageButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this@FlexRateInfo, FlexRateExerInfo::class.java)
            startActivity(intent)
            finish()
        }

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@FlexRateInfo, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}