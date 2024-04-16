package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class TonTwistInfoPage  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ton_twist_info)

        val nextButton: ImageButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this@TonTwistInfoPage, ChooseTongueTwisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@TonTwistInfoPage, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }



    }
}